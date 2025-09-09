package com.example.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.server.model.Auth;
import com.example.server.repository.AuthRepository;

@Service
public class AuthService {

    @Autowired
    private AuthRepository authRepository;

    public Auth getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Пользователь не авторизован");
        }

        String emailFromToken = authentication.getName(); // обычно это "sub" из JWT
        return authRepository.findByEmail(emailFromToken)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

}
