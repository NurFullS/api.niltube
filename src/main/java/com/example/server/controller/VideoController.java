package com.example.server.controller;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.server.dto.VideoUploadRequestDto;
import com.example.server.dto.VideoResponseDto;
import com.example.server.model.Auth;
import com.example.server.model.Video;
import com.example.server.repository.VideoRepository;
import com.example.server.service.AuthService;
import com.example.server.service.CloudinaryService;
import com.example.server.service.VideoService;

@RestController
@RequestMapping("/video")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class VideoController {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private AuthService authService;

    @Autowired
    private VideoService videoService;

    @PostMapping("/video-upload")
    public ResponseEntity<VideoResponseDto> uploadVideo(@ModelAttribute VideoUploadRequestDto request) {
        try {
            MultipartFile videoFile = request.getFile();
            if (videoFile == null || videoFile.isEmpty()) {
                throw new RuntimeException("Видео не было загружено");
            }

            MultipartFile previewFile = request.getFilePreview();
            if (previewFile == null || previewFile.isEmpty()) {
                throw new RuntimeException("Превью не было загружено");
            }

            String videoUrl = cloudinaryService.uploadFile(videoFile);
            String previewUrl = cloudinaryService.uploadImage(previewFile);

            Auth currentUser = authService.getCurrentUser();

            Video video = new Video();
            video.setVideoUrl(videoUrl);
            video.setVideoPreview(previewUrl);
            video.setVideoName(request.getVideoName());
            video.setVideoDescription(request.getVideoDescription());
            video.setOwner(currentUser);
            video.setCreatedAt(new Date());

            videoRepository.save(video);

            VideoResponseDto dto = new VideoResponseDto(video);
            if (currentUser != null) {
                dto.setOwnerUsername(currentUser.getUsername());
                dto.setOwnerEmail(currentUser.getEmail());
            }

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/by-name/{videoName}")
    public ResponseEntity<VideoResponseDto> getVideoByName(@PathVariable String videoName) {
        return videoRepository.findByVideoNameIgnoreCase(videoName)
                .map(video -> new VideoResponseDto(video))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-id/{id}")
    public ResponseEntity<VideoResponseDto> getVideo(@PathVariable Long id) {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Видео не найдено"));
        VideoResponseDto dto = new VideoResponseDto(video);
        if (video.getOwner() != null) {
            dto.setOwnerUsername(video.getOwner().getUsername());
            dto.setOwnerEmail(video.getOwner().getEmail());
        }
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/delete-video/{id}")
    public ResponseEntity<?> deleteVideo(@PathVariable Long id) {
        Optional<Video> optionalVideo = videoRepository.findById(id);

        if (optionalVideo.isEmpty()) {
            return ResponseEntity.status(404).body("Видео не найдено");
        }

        Video video = optionalVideo.get();

        try {
            if (video.getVideoUrl() != null) {
                cloudinaryService.deleteFile(video.getVideoUrl());
            }
            if (video.getVideoPreview() != null) {
                cloudinaryService.deleteFile(video.getVideoPreview());
            }

            videoRepository.deleteById(id);

            return ResponseEntity.ok("Видео успешно удалено");
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Ошибка при удалении видео: " + e.getMessage());
        }
    }

    @GetMapping("/videos")
    public ResponseEntity<List<VideoResponseDto>> getAllVideo() {
        List<Video> videos = videoRepository.findAll();

        List<VideoResponseDto> dtoList = videos.stream().map(video -> {
            VideoResponseDto dto = new VideoResponseDto(video);
            if (video.getOwner() != null) {
                dto.setOwnerUsername(video.getOwner().getUsername());
                dto.setOwnerEmail(video.getOwner().getEmail());
            }
            return dto;
        }).toList();

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/search")
    public List<VideoResponseDto> searchVideos(@RequestParam String query) {
        List<Video> videos = videoService.searchVideos(query, 10);
        return videos.stream().map(VideoResponseDto::new).collect(Collectors.toList());
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<VideoResponseDto>> getVideosByUser(@PathVariable String username) {
        List<Video> videos = videoRepository.findByOwnerUsername(username);

        List<VideoResponseDto> dtoList = videos.stream().map(video -> {
            VideoResponseDto dto = new VideoResponseDto(video);
            if (video.getOwner() != null) {
                dto.setOwnerUsername(video.getOwner().getUsername());
                dto.setOwnerEmail(video.getOwner().getEmail());
            }
            return dto;
        }).toList();

        return ResponseEntity.ok(dtoList);
    }

}
