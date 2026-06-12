package com.taskforge.controller;

import com.taskforge.common.ApiResponse;
import com.taskforge.dto.request.UpdateProfileRequest;
import com.taskforge.dto.response.UserResponse;
import com.taskforge.exception.ResourceNotFoundException;
import com.taskforge.model.User;
import com.taskforge.repository.UserRepository;
import com.taskforge.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private static final String PHOTO_DIR =
            System.getProperty("user.home") + "/AppData/Local/TaskForge/photos/";

    /** GET /api/users/me — ambil profil user yang sedang login */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(Authentication authentication) {
        User user = getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.success("Profil berhasil diambil", UserResponse.from(user)));
    }

    /** PUT /api/users/me — update nama dan NIM */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication) {

        User user = getCurrentUser(authentication);

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }
        // NIM boleh kosong (dihapus) tapi kalau diisi harus trimmed
        user.setNim(request.getNim() != null ? request.getNim().trim() : null);

        User saved = userRepository.save(user);
        log.info("Profil user {} diperbarui", saved.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Profil berhasil diperbarui", UserResponse.from(saved)));
    }

    /** POST /api/users/me/photo — upload foto profil */
    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        User user = getCurrentUser(authentication);

        // Pastikan direktori ada (createDirectories lebih reliable dari mkdirs di Windows)
        Path photoDir = Paths.get(PHOTO_DIR);
        Files.createDirectories(photoDir);

        // Simpan file dengan nama: user-{id}.{ext}
        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf('.'))
                : ".jpg";
        String fileName = "user-" + user.getId() + ext;
        Path target = photoDir.resolve(fileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        user.setPhotoPath(target.toAbsolutePath().toString());
        User saved = userRepository.save(user);
        log.info("Foto profil user {} diperbarui: {}", saved.getEmail(), target.toAbsolutePath());
        return ResponseEntity.ok(ApiResponse.success("Foto berhasil diupload", UserResponse.from(saved)));
    }

    private User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
    }
}
