package com.taskforge.repository;

import com.taskforge.model.ContributionScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContributionScoreRepository extends JpaRepository<ContributionScore, Long> {

    Optional<ContributionScore> findByUserIdAndProjectId(Long userId, Long projectId);

    List<ContributionScore> findByProjectIdOrderByScoreDesc(Long projectId);
}
