package com.taskforge.controller;

import com.taskforge.common.ApiResponse;
import com.taskforge.dto.response.ScoreResponse;
import com.taskforge.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/{projectId}/scores")
    @PreAuthorize("@projectService.isMember(#projectId, authentication.name)")
    public ResponseEntity<ApiResponse<List<ScoreResponse>>> getScores(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getScores(projectId)));
    }

    @GetMapping("/{projectId}/report")
    @PreAuthorize("hasRole('KETUA') and @projectService.isMember(#projectId, authentication.name)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReport(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.generateProjectReport(projectId)));
    }

    @GetMapping("/{projectId}/report/contribution")
    @PreAuthorize("@projectService.isMember(#projectId, authentication.name)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getContributionReport(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.generateContributionReport(projectId)));
    }
}
