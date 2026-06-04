package com.taskforge.report;

import com.taskforge.dto.response.ScoreResponse;
import com.taskforge.model.ContributionScore;
import com.taskforge.repository.ContributionScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Polymorphism: generate() produces a contribution-focused report (vs ProjectSummaryReport)
@Component
@RequiredArgsConstructor
public class ContributionReport extends ReportGenerator<List<ContributionScore>> {

    private final ContributionScoreRepository scoreRepository;

    @Override
    @Transactional(readOnly = true)
    protected List<ContributionScore> collectData(Long projectId) {
        return scoreRepository.findByProjectIdOrderByScoreDesc(projectId);
    }

    @Override
    protected Map<String, Object> formatReport(List<ContributionScore> scores) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("reportType", "CONTRIBUTION");
        report.put("generatedAt", LocalDateTime.now().toString());
        report.put("scores", ScoreResponse.fromList(scores));
        report.put("totalScore", scores.stream().mapToDouble(ContributionScore::getScore).sum());
        report.put("averageScore", scores.stream().mapToDouble(ContributionScore::getScore).average().orElse(0));
        return report;
    }
}
