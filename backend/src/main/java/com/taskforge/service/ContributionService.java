package com.taskforge.service;

import com.taskforge.model.ContributionScore;
import com.taskforge.model.Project;
import com.taskforge.model.User;
import com.taskforge.model.task.BaseTask;
import com.taskforge.repository.ContributionScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContributionService {

    private final ContributionScoreRepository scoreRepository;

    /**
     * REQ-SCORE-01: called every time a task transitions to DONE.
     * Formula: score = (priority_weight) + (ontime_bonus)
     *          ontime_bonus = +1 if completedAt < deadline, else 0
     *          percentage = (user_score / total_all_scores) × 100
     */
    @Transactional
    public void addScore(BaseTask task) {
        if (task.getAssignee() == null) return;

        User assignee = task.getAssignee();
        Project project = task.getProject();

        ContributionScore score = scoreRepository
                .findByUserIdAndProjectId(assignee.getId(), project.getId())
                .orElse(ContributionScore.builder()
                        .user(assignee)
                        .project(project)
                        .build());

        boolean onTime = task.getCompletedAt() != null
                && task.getCompletedAt().isBefore(task.getDeadline());

        score.setScore(score.getScore() + task.getScore());
        score.setTasksCompleted(score.getTasksCompleted() + 1);
        if (onTime) score.setTasksOntime(score.getTasksOntime() + 1);
        score.setLastUpdated(LocalDateTime.now());
        scoreRepository.save(score);

        recalculatePercentages(project);

        log.info("Score +{} untuk {} di proyek '{}' (on-time: {})",
                task.getScore(), assignee.getName(), project.getTitle(), onTime);
    }

    private void recalculatePercentages(Project project) {
        List<ContributionScore> all = scoreRepository.findByProjectIdOrderByScoreDesc(project.getId());
        double total = all.stream().mapToDouble(ContributionScore::getScore).sum();
        if (total == 0) return;
        all.forEach(s -> s.setPercentage((s.getScore() / total) * 100.0));
        scoreRepository.saveAll(all);
    }

    @Transactional
    public void addScoreForComment(User user, Project project) {
        ContributionScore score = scoreRepository
                .findByUserIdAndProjectId(user.getId(), project.getId())
                .orElse(ContributionScore.builder()
                        .user(user)
                        .project(project)
                        .build());
        
        score.setScore(score.getScore() + 2.5);
        score.setLastUpdated(LocalDateTime.now());
        scoreRepository.save(score);

        recalculatePercentages(project);
        
        log.info("Score +2.5 untuk {} di proyek '{}' (komentar)", user.getName(), project.getTitle());
    }

    @Transactional(readOnly = true)
    public List<ContributionScore> getScores(Long projectId) {
        return scoreRepository.findByProjectIdOrderByScoreDesc(projectId);
    }
}
