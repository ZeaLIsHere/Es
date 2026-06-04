package com.taskforge.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskAssignRequest {

    @NotNull(message = "Assignee wajib ditentukan")
    private Long assigneeId;
}
