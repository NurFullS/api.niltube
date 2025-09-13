package com.example.server.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class VideoUploadRequestDto {
    private MultipartFile file;
    private MultipartFile filePreview;
    private String videoName;
    private String videoDescription;
}