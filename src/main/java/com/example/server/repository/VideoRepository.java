package com.example.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.server.model.Video;

public interface VideoRepository extends JpaRepository<Video, Long> {
    
}
