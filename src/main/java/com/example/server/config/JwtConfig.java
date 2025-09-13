package com.example.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${JWT_KEY}")
    private String secret;

    public String getSecret() {
        return secret;
    }
}
