package com.example.server.dto;

import java.util.Date;

import com.example.server.model.Video;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoResponseDto {
    private Long id;
    private String videoUrl;
    private String videoPreview;
    private String videoName;
    private String videoDescription;
    private String ownerUsername;
    private String ownerEmail;
    private String ownerAvatar;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date createdAt;

    public VideoResponseDto(Video video) {
        this.id = video.getId();
        this.videoUrl = video.getVideoUrl();
        this.videoPreview = video.getVideoPreview();
        this.videoName = video.getVideoName();
        this.videoDescription = video.getVideoDescription();
        this.ownerUsername = video.getOwner() != null ? video.getOwner().getUsername() : null;
        this.ownerEmail = video.getOwner() != null ? video.getOwner().getEmail() : null;
        this.ownerAvatar = video.getOwner() != null ? video.getOwner().getAvatar() : null;
        this.createdAt = video.getCreatedAt();
    }
}