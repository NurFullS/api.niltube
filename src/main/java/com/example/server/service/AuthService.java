package com.example.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.server.config.JwtConfig;
import com.example.server.model.Auth;
import com.example.server.repository.AuthRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

@Service
public class AuthService {

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private JwtConfig jwtConfig;

    public Auth getCurrentUser() {
        String token = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            throw new RuntimeException("Пользователь не авторизован");
        }

        try {
            String emailFromToken = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            return authRepository.findByEmail(emailFromToken)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        } catch (Exception e) {
            throw new RuntimeException("Неверный токен");
        }
    }
}
