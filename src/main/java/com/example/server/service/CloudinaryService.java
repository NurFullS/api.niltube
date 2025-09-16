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
                "api_secret", "dhAhHNVpXSpMicpMIFLd8a4F3FM"));
    }

    public String uploadFile(MultipartFile file) throws IOException {
        var uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "videos",
                        "resource_type", "video"));
        return uploadResult.get("secure_url").toString();
    }

    public String uploadImage(MultipartFile file) throws IOException {
        var uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "avatars",
                        "resource_type", "image"));
        return uploadResult.get("secure_url").toString();
    }

    public void deleteFile(String fileUrl) {
        try {
            String publicId = extractPublicId(fileUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при удалении файла из Cloudinary: " + e.getMessage());
        }
    }

    private String extractPublicId(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty())
            return null;

        try {
            String[] parts = fileUrl.split("/");
            String filename = parts[parts.length - 1];
            String folder = parts[parts.length - 2];
            String publicId = folder + "/" + filename.substring(0, filename.lastIndexOf('.'));
            return publicId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
