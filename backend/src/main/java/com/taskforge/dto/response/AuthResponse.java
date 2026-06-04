package com.taskforge.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private String token;
    private long expiresIn;
    private UserResponse user;
}
