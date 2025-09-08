package com.jobhuntly.backend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

    public record CloudAsset(
            String secureUrl,
            String resourceType, // image | raw | video
            String format,
            Long bytes,
            String version // "v123..."
    ) {}

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

    // ===================== USER AVATAR (1-1, overwrite) =====================
    public CloudAsset uploadUserAvatar(Long userId, MultipartFile file) throws IOException {
        validateImage(file);
        String publicId = "users/" + userId + "/avatar";

        Map<?, ?> res = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", "image",
                        "overwrite", true,
                        "invalidate", true,
                        "folder", "users/" + userId
                )
        );
        return toAsset(res);
    }

    public void deleteUserAvatar(Long userId) throws IOException {
        destroyAllTypes("users/" + userId + "/avatar");
    }

    // ===================== COMPANY AVATAR (1-1, overwrite) =====================
    public CloudAsset uploadCompanyAvatar(Long companyId, MultipartFile file) throws IOException {
        validateImage(file);
        String publicId = "companies/" + companyId + "/avatar";

        Map<?, ?> res = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", "image",
                        "overwrite", true,
                        "invalidate", true,
                        "folder", "companies/" + companyId
                )
        );
        return toAsset(res);
    }

    public void deleteCompanyAvatar(Long companyId) throws IOException {
        destroyAllTypes("companies/" + companyId + "/avatar");
    }

    // ===================== APPLICATION CV (1-1, overwrite) =====================
    public CloudAsset uploadApplicationCv(Integer applicationId, MultipartFile file) throws IOException {
        validateCommon(file); // chỉ check rỗng + size
        String publicId = "applications/" + applicationId + "/cv";

        Map<?, ?> res = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", "raw",
                        "overwrite", true,
                        "invalidate", true
                )
        );
        return toAsset(res);
    }

    public void deleteApplicationCv(Integer applicationId) throws IOException {
        destroyAllTypes("applications/" + applicationId + "/cv");
    }

    // ===================== USER MULTI CVs (N-1) =====================
    /**
     * Tạo CV mới cho user (KHÔNG overwrite). Đặt vào folder users/{userId}/cvs.
     * Cloudinary sẽ tạo tên file duy nhất (unique_filename=true).
     */
    public CloudAsset uploadUserCv(Long userId, MultipartFile file, String displayName) throws IOException {
        validateCommon(file); // không check MIME
        String folder = "users/" + userId + "/cvs";

        Map<?, ?> res = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", "raw",
                        "use_filename", true,
                        "unique_filename", true,
                        "filename", safeFilename(displayName)
                )
        );
        return toAsset(res);
    }

    /**
     * Ghi đè 1 CV cụ thể theo URL hiện đang lưu (fallback khi cần update đúng file).
     * Trích public_id từ URL và overwrite.
     */
    public CloudAsset replaceUserCvByUrl(String existingUrl, MultipartFile newFile) throws IOException {
        validateCommon(newFile);
        String publicId = extractPublicIdFromUrl(existingUrl);
        if (publicId == null) throw new IOException("Không trích được public_id từ URL: " + existingUrl);

        Map<?, ?> res = cloudinary.uploader().upload(
                newFile.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", "raw",
                        "overwrite", true,
                        "invalidate", true
                )
        );
        return toAsset(res);
    }

    /**
     * Xoá 1 CV cụ thể theo URL (khi bạn chỉ lưu URL).
     */
    public void deleteUserCvByUrl(String url) throws IOException {
        String publicId = extractPublicIdFromUrl(url);
        if (publicId != null) destroyAllTypes(publicId);
    }

    // ===================== Helpers =====================
    private void destroyAllTypes(String publicId) throws IOException {
        try { cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image")); } catch (Exception ignored) {}
        try { cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw")); } catch (Exception ignored) {}
        try { cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "video")); } catch (Exception ignored) {}
    }

    private void validateImage(MultipartFile f) throws IOException {
        validateCommon(f);
        String ct = f.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new IOException("Only image files are allowed");
        }
    }

    private void validateCommon(MultipartFile f) throws IOException {
        if (f == null || f.isEmpty()) throw new IOException("Empty file");
        long max = 20L * 1024 * 1024; // 20MB
        if (f.getSize() > max) throw new IOException("File exceeds 20MB");
    }

    private CloudAsset toAsset(Map<?, ?> res) {
        String secureUrl   = (String) res.get("secure_url");
        String resourceType= (String) res.get("resource_type");
        String format      = (String) res.get("format");
        Long bytes         = (res.get("bytes") instanceof Number n) ? n.longValue() : null;
        String version     = (res.get("version") != null) ? "v" + res.get("version") : null;
        return new CloudAsset(secureUrl, resourceType, format, bytes, version);
    }

    private String safeFilename(String name) {
        if (name == null || name.isBlank()) return "cv";
        return name.replaceAll("[^a-zA-Z0-9-_\\.]+", "_");
    }

    // .../upload/(transforms/)?v123/.../folder/name.ext -> group(1) = folder/name
    private String extractPublicIdFromUrl(String url) {
        if (url == null) return null;
        Pattern p = Pattern.compile("/upload/(?:[^/]+/)*?(?:v\\d+/)?(.+?)\\.[^./?]+(?:\\?.*)?$");
        Matcher m = p.matcher(url);
        return m.find() ? m.group(1) : null;
    }
}
