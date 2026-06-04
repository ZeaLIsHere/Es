package com.taskforge.repository;

import com.taskforge.model.file.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {

    List<ProjectFile> findByTaskIdOrderByUploadedAtDesc(Long taskId);
}
