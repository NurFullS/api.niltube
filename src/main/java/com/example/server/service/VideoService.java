package com.example.server.service;

import com.example.server.model.Video;
import com.example.server.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;

    public List<Video> searchVideos(String query, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return videoRepository.searchVideos(query, pageable);
    }
}
