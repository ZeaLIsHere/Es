package com.taskforge.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
public class LinkedFileRequest {

    @NotBlank(message = "Nama link tidak boleh kosong")
    private String name;

    @NotBlank(message = "URL tidak boleh kosong")
    @URL(message = "Format URL tidak valid")
    private String url;

    private String description;
}
