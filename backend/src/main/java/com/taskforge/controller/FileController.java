package com.taskforge.controller;

import com.taskforge.common.ApiResponse;
import com.taskforge.dto.request.LinkedFileRequest;
import com.taskforge.dto.response.FileResponse;
import com.taskforge.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping("/api/tasks/{taskId}/files")
    public ResponseEntity<ApiResponse<List<FileResponse>>> getTaskFiles(@PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(fileService.getTaskFiles(taskId)));
    }

    @PostMapping("/api/tasks/{taskId}/files/upload")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            Authentication auth) {
        FileResponse response = fileService.upload(taskId, auth.getName(), file, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("File berhasil diupload", response));
    }

    @PostMapping("/api/tasks/{taskId}/files/link")
    public ResponseEntity<ApiResponse<FileResponse>> addLink(
            @PathVariable Long taskId,
            @Valid @RequestBody LinkedFileRequest request,
            Authentication auth) {
        FileResponse response = fileService.addLink(taskId, auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Link berhasil ditambahkan", response));
    }

    @GetMapping("/api/files/{fileId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long fileId) {
        return fileService.download(fileId);
    }

    // KETUA can delete any file; ANGGOTA only their own uploads
    @DeleteMapping("/api/files/{fileId}")
    @PreAuthorize("hasRole('KETUA') or @fileService.isUploader(#fileId, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable Long fileId,
            Authentication auth) {
        fileService.delete(fileId, auth.getName());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(null));
    }
}
