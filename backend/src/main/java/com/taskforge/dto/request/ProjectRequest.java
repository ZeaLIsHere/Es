package com.taskforge.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectRequest {

    @NotBlank(message = "Judul proyek tidak boleh kosong")
    @Size(max = 200, message = "Judul proyek maksimal 200 karakter")
    private String title;

    private String description;

    private LocalDateTime deadline;

    @NotNull(message = "Max anggota wajib diisi")
    @Min(value = 2, message = "Max anggota minimal 2 (termasuk ketua)")
    @Max(value = 20, message = "Max anggota maksimal 20")
    private Integer maxMembers;
}
