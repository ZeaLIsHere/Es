package com.taskforge.dto.response;

import com.taskforge.model.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private String nim;
    private String photoPath;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .nim(user.getNim())
                .photoPath(user.getPhotoPath())
                .build();
    }
}
