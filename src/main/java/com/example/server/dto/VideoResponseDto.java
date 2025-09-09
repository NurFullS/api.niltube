package com.example.server.dto;

import com.example.server.model.Video;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoResponseDto {
    private Long id;
    private String videoUrl;
    private String videoName;
    private String videoDescription;
    private String ownerUsername;
    private String ownerEmail;

    public VideoResponseDto(Video video) {
        this.id = video.getId();
        this.videoUrl = video.getVideoUrl();
        this.videoName = video.getVideoName();
        this.videoDescription = video.getVideoDescription();
        this.ownerUsername = (video.getOwner() != null) ? video.getOwner().getUsername() : null;
    }
}
