package com.taskforge.service;

import com.taskforge.dto.request.ChangePasswordRequest;
import com.taskforge.dto.request.UpdateProfileRequest;
import com.taskforge.dto.response.ProfileUpdateResponse;
import com.taskforge.dto.response.UserResponse;
import com.taskforge.exception.DuplicateResourceException;
import com.taskforge.exception.ResourceNotFoundException;
import com.taskforge.exception.ValidationException;
import com.taskforge.model.User;
import com.taskforge.repository.UserRepository;
import com.taskforge.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${file.photo-dir}")
    private String photoDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    public UserResponse getProfile(String email) {
        return UserResponse.from(findByEmail(email));
    }

    @Transactional
    public ProfileUpdateResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findByEmail(email);
        String newEmail = request.getEmail().trim().toLowerCase();

        if (!user.getEmail().equalsIgnoreCase(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new DuplicateResourceException("Email sudah digunakan");
        }

        user.setName(request.getName().trim());
        user.setEmail(newEmail);
        user.setNim(request.getNim() != null && !request.getNim().isBlank()
                ? request.getNim().trim() : null);
        User saved = userRepository.save(user);
        log.info("Profil user {} diperbarui", saved.getEmail());

        UserResponse userResponse = UserResponse.from(saved);
        String newToken = null;
        if (!email.equalsIgnoreCase(saved.getEmail())) {
            newToken = jwtUtil.generateToken(saved.getId(), saved.getEmail(), saved.getRole().name());
        }

        return ProfileUpdateResponse.builder()
                .user(userResponse)
                .token(newToken)
                .build();
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findByEmail(email);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Password lama tidak sesuai");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ValidationException("Password baru harus berbeda dari password lama");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password user {} diperbarui", user.getEmail());
    }

    @Transactional
    public UserResponse uploadPhoto(String email, MultipartFile file) throws IOException {
        validateImageFile(file);

        User user = findByEmail(email);

        Path dir = Paths.get(photoDir).toAbsolutePath().normalize();
        Files.createDirectories(dir);

        String ext = extractExtension(file.getOriginalFilename());
        String fileName = "user-" + user.getId() + ext;
        Path target = dir.resolve(fileName).normalize();

        // Cegah path traversal
        if (!target.startsWith(dir)) {
            throw new ValidationException("Nama file tidak valid");
        }

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // Simpan nama file saja — bukan absolute path
        user.setPhotoPath(fileName);
        User saved = userRepository.save(user);
        log.info("Foto profil user {} diperbarui", saved.getEmail());
        return UserResponse.from(saved);
    }

    public record PhotoFile(byte[] data, MediaType mediaType) {}

    public PhotoFile getPhoto(String email) throws IOException {
        User user = findByEmail(email);
        String fileName = user.getPhotoPath();
        if (fileName == null || fileName.isBlank()) {
            throw new ResourceNotFoundException("Foto tidak ditemukan");
        }
        Path dir = Paths.get(photoDir).toAbsolutePath().normalize();
        Path photoFile = dir.resolve(fileName).normalize();

        // Cegah path traversal dari data yang tersimpan di DB
        if (!photoFile.startsWith(dir) || !Files.exists(photoFile)) {
            throw new ResourceNotFoundException("Foto tidak ditemukan");
        }

        MediaType mediaType = fileName.toLowerCase().endsWith(".png")
                ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
        return new PhotoFile(Files.readAllBytes(photoFile), mediaType);
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File tidak boleh kosong");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new ValidationException("Tipe file tidak didukung. Gunakan JPG atau PNG");
        }
        extractExtension(file.getOriginalFilename());
    }

    private String extractExtension(String originalName) {
        if (originalName == null || !originalName.contains(".")) return ".jpg";
        String ext = originalName.substring(originalName.lastIndexOf('.')).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ValidationException("Ekstensi file tidak didukung. Gunakan .jpg, .jpeg, atau .png");
        }
        return ext;
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
    }
}
