package com.taskforge.dto.request;

import com.taskforge.model.task.BaseTask.Priority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskRequest {

    @NotBlank(message = "Judul task tidak boleh kosong")
    @Size(max = 200, message = "Judul task maksimal 200 karakter")
    private String title;

    private String description;

    @NotNull(message = "Prioritas wajib dipilih")
    private Priority priority;

    @NotNull(message = "Deadline wajib diisi")
    @Future(message = "Deadline harus di masa depan")
    private LocalDateTime deadline;

    @NotNull(message = "Assignee wajib ditentukan")
    private Long assigneeId;

    @NotNull(message = "Tipe task wajib dipilih")
    private TaskType taskType;

    public enum TaskType {
        SIMPLE, MILESTONE
    }
}
