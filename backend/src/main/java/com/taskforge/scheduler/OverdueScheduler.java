package com.taskforge.scheduler;

import com.taskforge.model.task.BaseTask;
import com.taskforge.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueScheduler {

    private final TaskRepository taskRepository;

    // REQ-TASK-06: run every minute, flag tasks past deadline that are not DONE
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void checkOverdueTasks() {
        List<BaseTask> overdue = taskRepository.findOverdueTasks(LocalDateTime.now());
        if (!overdue.isEmpty()) {
            overdue.forEach(BaseTask::markOverdue);
            taskRepository.saveAll(overdue);
            log.info("Overdue check: {} task(s) ditandai overdue", overdue.size());
        }
    }
}
