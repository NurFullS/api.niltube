package com.example.server.repository;

import com.example.server.model.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AuthRepository extends JpaRepository<Auth, Long> {

    Optional<Auth> findByEmail(String email);
}