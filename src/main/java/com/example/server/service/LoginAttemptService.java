package com.example.server.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_MS = 60_000;

    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    public void loginFailed(String email) {
        AttemptInfo info = attempts.getOrDefault(email, new AttemptInfo());
        info.count++;
        if (info.count >= MAX_ATTEMPTS) {
            info.blockedUntil = System.currentTimeMillis() + LOCK_TIME_MS;
        }
        attempts.put(email, info);
    }

    public void loginSucceeded(String email) {
        attempts.remove(email);
    }

    public boolean isBlocked(String email) {
        AttemptInfo info = attempts.get(email);
        if (info == null) return false;
        if (info.blockedUntil == 0) return false;
        if (info.blockedUntil > System.currentTimeMillis()) {
            return true;
        } else {
            attempts.remove(email);
            return false;
        }
    }

    private static class AttemptInfo {
        int count = 0;
        long blockedUntil = 0;
    }
}
