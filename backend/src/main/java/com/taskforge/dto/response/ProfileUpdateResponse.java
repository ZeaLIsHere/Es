package com.taskforge.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileUpdateResponse {

    private UserResponse user;
    private String token;
}
