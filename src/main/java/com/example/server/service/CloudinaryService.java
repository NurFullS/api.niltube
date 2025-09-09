package com.example.server.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dop4mtq1t",
            "api_key", "293689374536852",
            "api_secret", "dhAhHNVpXSpMicpMIFLd8a4F3FM"
        ));
    }

    public String uploadFile(MultipartFile file) throws IOException {
        var uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", "avatars"));
        return uploadResult.get("secure_url").toString();
    }
}