package com.taskforge.repository;

import com.taskforge.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByProjectIdOrderByTimestampDesc(Long projectId);

    List<ActivityLog> findByProjectIdInOrderByTimestampDesc(Collection<Long> projectIds);
}
