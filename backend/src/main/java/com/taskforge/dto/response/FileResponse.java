package com.taskforge.dto.response;

import com.taskforge.model.file.ProjectFile;
import com.taskforge.model.file.UploadedFile;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FileResponse {

    private Long id;
    private String name;
    private String fileType;      // "UPLOAD" or "LINK"
    private String accessUrl;     // polymorphic — download path or external URL
    private Long fileSize;
    private String mimeType;
    private String description;
    private LocalDateTime uploadedAt;
    private UserResponse uploadedBy;
    private Long taskId;

    public static FileResponse from(ProjectFile file) {
        Long size = null;
        String mime = null;
        if (file instanceof UploadedFile uf) {
            size = uf.getFileSize();
            mime = uf.getMimeType();
        }

        return FileResponse.builder()
                .id(file.getId())
                .name(file.getName())
                .fileType(file instanceof UploadedFile ? "UPLOAD" : "LINK")
                .accessUrl(file.getAccessUrl())      // Polymorphism: returns right URL per subtype
                .fileSize(size)
                .mimeType(mime)
                .description(file.getDescription())
                .uploadedAt(file.getUploadedAt())
                .uploadedBy(UserResponse.from(file.getUploadedBy()))
                .taskId(file.getTask() != null ? file.getTask().getId() : null)
                .build();
    }
}
