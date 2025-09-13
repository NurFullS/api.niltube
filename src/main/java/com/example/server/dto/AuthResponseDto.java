package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDto {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String role;

    public AuthResponseDto(Long id, String username, String email, String avatar) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
    }

    public AuthResponseDto() {}
}
