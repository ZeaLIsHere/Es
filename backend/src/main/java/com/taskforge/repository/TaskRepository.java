package com.taskforge.repository;

import com.taskforge.model.task.BaseTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<BaseTask, Long> {

    List<BaseTask> findByProjectId(Long projectId);

    @Query("SELECT t FROM BaseTask t WHERE t.deadline < :now AND t.status != 'DONE' AND t.overdue = false")
    List<BaseTask> findOverdueTasks(@Param("now") LocalDateTime now);
}
