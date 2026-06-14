package com.taskforge.service;

import com.taskforge.dto.request.MemberRequest;
import com.taskforge.dto.request.ProjectRequest;
import com.taskforge.dto.response.ProjectResponse;
import com.taskforge.dto.response.UserResponse;
import com.taskforge.exception.DuplicateResourceException;
import com.taskforge.exception.ResourceNotFoundException;
import com.taskforge.exception.ValidationException;
import com.taskforge.model.ActivityLog.ActionType;
import com.taskforge.model.Project;
import com.taskforge.model.User;
import com.taskforge.repository.ProjectRepository;
import com.taskforge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    // ── SpEL helpers (called from @PreAuthorize) ──────────────────────────

    @Transactional(readOnly = true)
    public boolean isMember(Long projectId, String email) {
        return projectRepository.findById(projectId)
                .map(p -> p.getOwner().getEmail().equals(email)
                        || p.getMembers().stream().anyMatch(m -> m.getEmail().equals(email)))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Long projectId, String email) {
        return projectRepository.findById(projectId)
                .map(p -> p.getOwner().getEmail().equals(email))
                .orElse(false);
    }

    // ── CRUD ──────────────────────────────────────────────────────────────

    @Transactional
    public ProjectResponse createProject(String actorEmail, ProjectRequest request) {
        User owner = getUser(actorEmail);

        Project project = Project.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .maxMembers(request.getMaxMembers())
                .owner(owner)
                .build();

        Project saved = projectRepository.save(project);
        log.info("Proyek '{}' dibuat oleh {}", saved.getTitle(), actorEmail);
        return ProjectResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getMyProjects(String actorEmail) {
        User user = getUser(actorEmail);
        return projectRepository.findAllByMemberOrOwner(user).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAvailableProjects(String actorEmail) {
        User user = getUser(actorEmail);
        return projectRepository.findAvailableFor(user).stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, String actorEmail) {
        Project project = getProject(projectId);
        ensureMember(project, actorEmail);
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, String actorEmail, ProjectRequest request) {
        Project project = getProject(projectId);
        ensureOwner(project, actorEmail);

        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setDeadline(request.getDeadline());

        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long projectId, String actorEmail) {
        Project project = getProject(projectId);
        ensureOwner(project, actorEmail);
        projectRepository.delete(project);
        log.info("Proyek id={} dihapus oleh {}", projectId, actorEmail);
    }

    @Transactional
    public UserResponse addMember(Long projectId, String actorEmail, MemberRequest request) {
        Project project = getProject(projectId);
        ensureOwner(project, actorEmail);

        User newMember = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User dengan email " + request.getEmail() + " tidak ditemukan"));

        if (project.getOwner().getId().equals(newMember.getId())
                || project.getMembers().contains(newMember)) {
            throw new DuplicateResourceException(
                    request.getEmail() + " sudah menjadi anggota proyek ini");
        }

        int currentCount = 1 + project.getMembers().size();
        int maxMembers = project.getMaxMembers() != null ? project.getMaxMembers() : 4;
        if (currentCount >= maxMembers) {
            throw new ValidationException(
                    "Proyek sudah penuh (" + maxMembers + " anggota termasuk ketua)");
        }

        project.getMembers().add(newMember);
        projectRepository.save(project);

        User actor = getUser(actorEmail);
        activityLogService.log(project, actor,
                actor.getName() + " menambahkan " + newMember.getName() + " ke proyek",
                ActionType.MEMBER_ADDED);

        log.info("Member {} ditambahkan ke proyek id={}", newMember.getEmail(), projectId);
        return UserResponse.from(newMember);
    }

    // ── Internal helpers ──────────────────────────────────────────────────

    public User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan: " + email));
    }

    public Project getProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
    }

    private void ensureMember(Project project, String email) {
        boolean ok = project.getOwner().getEmail().equals(email)
                || project.getMembers().stream().anyMatch(m -> m.getEmail().equals(email));
        if (!ok) throw new ResourceNotFoundException("Project", project.getId());
    }

    private void ensureOwner(Project project, String email) {
        if (!project.getOwner().getEmail().equals(email)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Hanya owner proyek yang bisa melakukan aksi ini");
        }
    }
}
