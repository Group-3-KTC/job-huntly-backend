package com.jobhuntly.backend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
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
            String version, // "v123..."
            String viewUrl,      // URL mở trực tiếp (inline)
            String downloadUrl   // URL ép tải xuống (signed)
    ) {
        public CloudAsset(String secureUrl,
                          String resourceType,
                          String format,
                          Long bytes,
                          String version) {
            this(secureUrl, resourceType, format, bytes, version, secureUrl, null);
        }
    }

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
        validateCompanyImage(file, "avatar");
        String publicId = "companies/" + companyId + "/avatar";

        Map<?, ?> res = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", "image",
                        "overwrite", true,
                        "invalidate", true,
                        "folder", "companies/" + companyId,
                        "transformation", new Transformation()
                                .width(300).height(300)
                                .crop("fill")
                                .quality("auto")
                                .fetchFormat("auto")
                )
        );
        return toAsset(res);
    }

    public void deleteCompanyAvatar(Long companyId) throws IOException {
        destroyAllTypes("companies/" + companyId + "/avatar");
    }

    // ===================== COMPANY COVER IMAGE (1-1, overwrite) =====================
    public CloudAsset uploadCompanyCover(Long companyId, MultipartFile file) throws IOException {
        validateCompanyImage(file, "cover");
        String publicId = "companies/" + companyId + "/cover";

        Map<?, ?> res = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", "image",
                        "overwrite", true,
                        "invalidate", true,
                        "folder", "companies/" + companyId,
                        "transformation", new Transformation()
                                .width(1200).height(400)
                                .crop("fill")
                                .quality("auto")
                                .fetchFormat("auto")
                )
        );
        return toAsset(res);
    }

    public void deleteCompanyCover(Long companyId) throws IOException {
        destroyAllTypes("companies/" + companyId + "/cover");
    }

    // ===================== COMPANY IMAGES BATCH UPLOAD =====================
    public record CompanyImageUploadResult(
            CloudAsset avatar,
            CloudAsset cover,
            String message
    ) {}

    /**
     * Upload cả avatar và cover cho company cùng lúc
     */
    public CompanyImageUploadResult uploadCompanyImages(Long companyId, 
                                                       MultipartFile avatarFile, 
                                                       MultipartFile coverFile) throws IOException {
        CloudAsset avatar = null;
        CloudAsset cover = null;
        StringBuilder message = new StringBuilder();

        try {
            if (avatarFile != null && !avatarFile.isEmpty()) {
                avatar = uploadCompanyAvatar(companyId, avatarFile);
                message.append("Avatar uploaded successfully. ");
            }

            if (coverFile != null && !coverFile.isEmpty()) {
                cover = uploadCompanyCover(companyId, coverFile);
                message.append("Cover image uploaded successfully. ");
            }

            if (avatar == null && cover == null) {
                throw new IOException("No valid image files provided");
            }

            return new CompanyImageUploadResult(avatar, cover, message.toString().trim());
        } catch (IOException e) {
            // Rollback nếu có lỗi
            if (avatar != null) {
                try { deleteCompanyAvatar(companyId); } catch (Exception ignored) {}
            }
            if (cover != null) {
                try { deleteCompanyCover(companyId); } catch (Exception ignored) {}
            }
            throw e;
        }
    }

    /**
     * Xóa tất cả hình ảnh của company
     */
    public void deleteAllCompanyImages(Long companyId) throws IOException {
        try {
            deleteCompanyAvatar(companyId);
        } catch (Exception e) {
            // Log error but continue
        }
        
        try {
            deleteCompanyCover(companyId);
        } catch (Exception e) {
            // Log error but continue
        }
    }

    // ===================== APPLICATION CV (1-1, overwrite) =====================
    public CloudAsset uploadApplicationCv(Integer applicationId, MultipartFile file) throws IOException {
        // 1. Kiểm tra rỗng
        validateCommon(file);

        // 2. Giới hạn dung lượng (5 MB)
        long maxSize = 5 * 1024 * 1024; // 5 MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File vượt quá giới hạn 5MB.");
        }

        // 3. Kiểm tra định dạng (chỉ PDF)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new IllegalArgumentException("Chỉ được phép upload file PDF.");
        }

        // 4. Tạo publicId cho Cloudinary
        String publicId = "applications/%d/cv.pdf".formatted(applicationId);

        // 5. Upload lên Cloudinary
        Map<?, ?> res = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "upload_preset", "public_cv",
                        "public_id", publicId,
                        "resource_type", "raw",
                        "overwrite", true,
                        "invalidate", true,
                        "access_mode", "public"
                )
        );

        // 6. Lấy thông tin trả về
        String secureUrl   = (String) res.get("secure_url");
        String resourceType = (String) res.get("resource_type");
        String format       = (String) res.get("format");
        Long bytes          = (res.get("bytes") instanceof Number n) ? n.longValue() : null;
        String version      = (res.get("version") != null) ? "v" + res.get("version") : null;

        return new CloudAsset(secureUrl, resourceType, format, bytes, version);
    }

    public void deleteApplicationCv(Integer applicationId) throws IOException {
        destroyAllTypes("applications/" + applicationId + "/cv");
    }

    // ===================== CV TEMPLATES (Admin manages many CVs)
    // =====================

    /**
     * Upload file cho CV template (create + update).
     * public_id = cv_templates/{cvId}/{type}
     * type: "html" hoặc "preview"
     */
    public CloudAsset uploadCvTemplateFile(Long cvId, MultipartFile file, String type) throws IOException {
        validateCommon(file);
        String publicId = "cv_templates/" + cvId + "/" + type;

        try {
            Map<?, ?> res = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "overwrite", true,
                            "invalidate", true,
                            "resource_type", "auto"));
            return toAsset(res);
        } catch (Exception e) {
            throw new IOException("Failed to upload CV template file to Cloudinary", e);
        }
    }

    /**
     * Xóa file CV template theo URL.
     */
    public void deleteCvTemplateFileByUrl(String url) throws IOException {
        try {
            String publicId = extractPublicIdFromUrl(url);
            if (publicId != null) {
                destroyAllTypes(publicId);
            }
        } catch (Exception e) {
            throw new IOException("Failed to delete CV template file from Cloudinary", e);
        }
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
    
    /**
     * Validation chuyên biệt cho hình ảnh company
     */
    private void validateCompanyImage(MultipartFile file, String type) throws IOException {
        validateImage(file);
        
        // Kiểm tra kích thước file (tối đa 5MB cho company images)
        long maxSize = 5L * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IOException("Company " + type + " image must not exceed 5MB");
        }
        
        // Kiểm tra định dạng file
        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.equals("image/jpeg") && 
             !contentType.equals("image/jpg") && 
             !contentType.equals("image/png") && 
             !contentType.equals("image/webp"))) {
            throw new IOException("Company " + type + " must be JPEG, JPG, PNG, or WebP format");
        }
        
        // Kiểm tra tên file
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IOException("Invalid file name");
        }
        
        // Kiểm tra kích thước tối thiểu (avatar: 100x100, cover: 600x200)
        if ("avatar".equals(type)) {
            // Avatar nên có tỷ lệ vuông hoặc gần vuông
            // Cloudinary sẽ tự động resize về 300x300
        } else if ("cover".equals(type)) {
            // Cover image nên có tỷ lệ 3:1 hoặc tương tự
            // Cloudinary sẽ tự động resize về 1200x400
        }
    }
}
