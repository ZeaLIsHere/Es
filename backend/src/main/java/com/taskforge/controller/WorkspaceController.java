package com.taskforge.controller;

import com.taskforge.common.ApiResponse;
import com.taskforge.dto.response.WorkspaceDataResponse;
import com.taskforge.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/workspace")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping("/data")
    public ResponseEntity<ApiResponse<WorkspaceDataResponse>> getWorkspaceData(Authentication auth) {
        WorkspaceDataResponse data = workspaceService.getWorkspaceData(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
