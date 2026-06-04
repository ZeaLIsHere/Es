package com.taskforge.ui.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileModel {

    private Long id;
    private String name;
    private String fileType;   // "UPLOAD" or "LINK"
    private String accessUrl;
    private Long fileSize;
    private String mimeType;
    private String description;
    private LocalDateTime uploadedAt;
    private UserModel uploadedBy;
    private Long taskId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public String getAccessUrl() { return accessUrl; }
    public void setAccessUrl(String accessUrl) { this.accessUrl = accessUrl; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public UserModel getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(UserModel uploadedBy) { this.uploadedBy = uploadedBy; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public boolean isLink() { return "LINK".equals(fileType); }

    public String getTypeIcon() {
        if (isLink()) return "🔗";
        if (name == null) return "📄";
        String lower = name.toLowerCase();
        if (lower.endsWith(".pdf")) return "📕";
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return "📘";
        if (lower.endsWith(".ppt") || lower.endsWith(".pptx")) return "📙";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif")) return "🖼";
        if (lower.endsWith(".zip") || lower.endsWith(".rar")) return "📦";
        return "📄";
    }

    public String getFileSizeLabel() {
        if (fileSize == null) return "";
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1_048_576) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / 1_048_576.0);
    }

    public String getUploadedAtLabel() {
        if (uploadedAt == null) return "";
        return uploadedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
    }

    public String getUploaderName() {
        return uploadedBy != null ? uploadedBy.getName() : "—";
    }
}
