package com.jobhuntly.backend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) throws IOException {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "auto" // Tự động detect loại file (image, video, raw)
            ));
            return uploadResult.get("secure_url").toString(); // Trả về URL
        } catch (Exception e) {
            throw new IOException("Failed to upload file to Cloudinary", e);
        }
    }

    // Phương thức xóa file nếu cần
    public void deleteFile(String publicId) throws IOException {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new IOException("Failed to delete file from Cloudinary", e);
        }
    }
}
