package com.taskforge.model.file;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@DiscriminatorValue("UPLOAD")
@Getter
@Setter
@NoArgsConstructor
public class UploadedFile extends ProjectFile {

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    // Polymorphism: physical file access goes through the download endpoint
    @Override
    public String getAccessUrl() {
        return "/api/files/" + getId() + "/download";
    }
}
