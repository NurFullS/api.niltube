package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthDto {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String role;
}