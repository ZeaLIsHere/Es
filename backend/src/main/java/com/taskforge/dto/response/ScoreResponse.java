package com.taskforge.dto.response;

import com.taskforge.model.ContributionScore;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ScoreResponse {

    private Long userId;
    private String userName;
    private String userEmail;
    private double score;
    private int tasksCompleted;
    private int tasksOntime;
    private double percentage;
    private String scoreLevel; // "HIGH" | "MEDIUM" | "LOW" — drives bar color

    public static List<ScoreResponse> fromList(List<ContributionScore> scores) {
        if (scores.isEmpty()) return List.of();

        double avg = scores.stream().mapToDouble(ContributionScore::getScore).average().orElse(0);

        return scores.stream().map(s -> {
            String level;
            if (s.getScore() >= avg) level = "HIGH";
            else if (s.getScore() >= avg * 0.5) level = "MEDIUM";
            else level = "LOW";

            return ScoreResponse.builder()
                    .userId(s.getUser().getId())
                    .userName(s.getUser().getName())
                    .userEmail(s.getUser().getEmail())
                    .score(s.getScore())
                    .tasksCompleted(s.getTasksCompleted())
                    .tasksOntime(s.getTasksOntime())
                    .percentage(s.getPercentage())
                    .scoreLevel(level)
                    .build();
        }).toList();
    }
}
