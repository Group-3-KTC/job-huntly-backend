package com.jobhuntly.backend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
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
        String publicId = userId + "/avatar";

        Map<?, ?> res = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id", publicId,
                        "resource_type", "image",
                        "overwrite", true,
                        "invalidate", true,
                        "folder", "users/"
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

    private void validateCompanyImage(MultipartFile file, String type) throws IOException {
        validateImage(file);

        long maxSize = 5L * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IOException("Company " + type + " image must not exceed 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.equals("image/jpeg") && 
             !contentType.equals("image/jpg") && 
             !contentType.equals("image/png") && 
             !contentType.equals("image/webp"))) {
            throw new IOException("Company " + type + " must be JPEG, JPG, PNG, or WebP format");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IOException("Invalid file name");
        }

        if ("avatar".equals(type)) {
        } else if ("cover".equals(type)) {
        }
    }

    public UploadResult uploadBytes(byte[] data, String folder, String filename, String resourceType) {
        try {
            if (data == null || data.length == 0) {
                throw new IllegalArgumentException("Empty data");
            }

            String rt = normalizeResourceType(resourceType);

            String folderSafe = trimSlashesOrNull(folder);
            String base = asciiPublicIdBase(filename);
            String suffix = java.util.UUID.randomUUID().toString().substring(0, 8);

            String publicId = (folderSafe != null ? folderSafe + "/" : "") + base + "-" + suffix;

            @SuppressWarnings({"rawtypes","unchecked"})
            Map options = ObjectUtils.asMap(
                    "public_id",        publicId,
                    "resource_type",    rt,
                    "overwrite",        false,
                    "unique_filename",  false,
                    "secure",           true
            );

            @SuppressWarnings("rawtypes")
            Map res = cloudinary.uploader().upload(data, options);

            String returnedPublicId = (String) res.get("public_id");
            String secureUrl        = (String) res.get("secure_url");
            return new UploadResult(returnedPublicId, secureUrl);

        } catch (Exception e) {
            throw new IllegalStateException("Cloudinary upload failed", e);
        }
    }

    private static String normalizeResourceType(String rt) {
        if (rt == null) return "raw";
        rt = rt.trim().toLowerCase();
        return switch (rt) {
            case "image" -> "image";
            case "video" -> "video";
            case "raw"   -> "raw";
            default      -> "raw";
        };
    }

    private static String trimSlashesOrNull(String s) {
        if (s == null) return null;
        s = s.trim().replaceAll("^/+", "").replaceAll("/+$", "");
        return s.isEmpty() ? null : s;
    }

    private static String asciiPublicIdBase(String filename) {
        String base = (filename != null && !filename.isBlank()) ? filename : "file";
        base = base.replaceAll("[\\\\/]+", "_");
        int dot = base.lastIndexOf('.');
        if (dot > 0) base = base.substring(0, dot);

        base = java.text.Normalizer.normalize(base, java.text.Normalizer.Form.NFKD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("[^A-Za-z0-9._-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("(^_|_$)", "");

        if (base.isBlank()) base = "file";
        if (base.length() > 80) base = base.substring(0, 80);
        return base;
    }

    public record UploadResult(String publicId, String secureUrl) {}
}
