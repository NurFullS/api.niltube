package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDto {
    private Long id;          // id пользователя
    private String username;  // имя пользователя
    private String email;     // email
    private String avatar;    // аватар
    private String role;      // роль (USER, CREATOR, ADMIN)
}
