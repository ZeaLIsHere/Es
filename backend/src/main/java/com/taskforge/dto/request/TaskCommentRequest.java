package com.taskforge.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskCommentRequest {
    @NotBlank(message = "Komentar tidak boleh kosong")
    private String content;
}
