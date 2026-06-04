package com.taskforge.dto.request;

import com.taskforge.model.task.BaseTask.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusRequest {

    @NotNull(message = "Status tidak boleh kosong")
    private TaskStatus status;
}
