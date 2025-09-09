package com.example.server.controller;

// import java.io.IOException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.server.dto.VideoUploadRequestDto;
import com.example.server.dto.VideoResponseDto;
import com.example.server.model.Auth;
import com.example.server.model.Video;
// import com.example.server.repository.AuthRepository;
import com.example.server.repository.VideoRepository;
import com.example.server.service.AuthService;
import com.example.server.service.CloudinaryService;

@RestController
@RequestMapping("/video")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class VideoController {

    @Autowired
    private VideoRepository videoRepository;

    // @Autowired
    // private AuthRepository authRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private AuthService authService;

    @PostMapping("/video-upload")
    public ResponseEntity<VideoResponseDto> uploadVideo(@ModelAttribute VideoUploadRequestDto request) {
        try {
            MultipartFile videoFile = request.getFile();
            if (videoFile == null || videoFile.isEmpty()) {
                throw new RuntimeException("Файл не был загружен");
            }

            String videoUrl = cloudinaryService.uploadFile(videoFile);
            Auth currentUser = authService.getCurrentUser();

            Video video = new Video();
            video.setVideoUrl(videoUrl);
            video.setVideoName(request.getVideoName());
            video.setVideoDescription(request.getVideoDescription());
            video.setOwner(currentUser);
            video.setCreatedAt(new Date());

            videoRepository.save(video);

            return ResponseEntity.ok(new VideoResponseDto(video));
        } catch (Exception e) {
            e.printStackTrace(); // ← здесь появится точная причина ошибки
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoResponseDto> getVideo(@PathVariable Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Видео не найдено"));

        VideoResponseDto dto = new VideoResponseDto(video);
        dto.setOwnerUsername(video.getOwner().getUsername());
        dto.setOwnerEmail(video.getOwner().getEmail());

        return ResponseEntity.ok(dto);
    }

}
