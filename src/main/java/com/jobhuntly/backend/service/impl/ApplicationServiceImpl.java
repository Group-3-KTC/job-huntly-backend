package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.ApplicationRequest;
import com.jobhuntly.backend.dto.response.ApplicationByUserResponse;
import com.jobhuntly.backend.dto.response.ApplicationResponse;
import com.jobhuntly.backend.entity.Application;
import com.jobhuntly.backend.entity.Job;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.mapper.ApplicationMapper;
import com.jobhuntly.backend.repository.ApplicationRepository;
import com.jobhuntly.backend.repository.JobRepository;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.service.ApplicationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
@Transactional
public class ApplicationServiceImpl implements ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final ApplicationMapper applicationMapper;
    private final CloudinaryService cloudinaryService;
    @Override
    public ApplicationResponse create(Long userId, ApplicationRequest req) {
        if (req.getJobId() == null) {
            throw new IllegalArgumentException("jobId là bắt buộc.");
        }

        // Chặn nộp trùng
        if (applicationRepository.existsByUser_IdAndJob_Id(userId, req.getJobId())) {
            throw new IllegalStateException("Bạn đã ứng tuyển job này rồi.");
        }

        // Map entity + set quan hệ
        Application app = applicationMapper.toEntity(req);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User không tồn tại"));
        Job job = jobRepository.findById(req.getJobId())
                .orElseThrow(() -> new EntityNotFoundException("Job không tồn tại"));

        app.setUser(user);
        app.setJob(job);

        // Để @PrePersist tự set "Applied"
        app.setStatus(null);

        // 1) Lưu trước để có appId (dùng cho public_id Cloudinary: applications/{appId}/cv)
        applicationRepository.saveAndFlush(app); // đảm bảo có ID ngay tại đây

        // 2) Upload CV nếu có
        MultipartFile cvFile = req.getCvFile();
        boolean uploadedCv = false;
        if (cvFile != null && !cvFile.isEmpty()) {
            try {
                var asset = cloudinaryService.uploadApplicationCv(app.getId(), cvFile); // overwrite theo appId
                app.setCv(asset.secureUrl()); // giữ cột 'cv' (URL) như hiện tại
                uploadedCv = true;
            } catch (Exception e) {
                // Ném lỗi để rollback DB; asset chưa được tạo thì không cần dọn
                throw new IllegalStateException("Upload CV thất bại. Vui lòng thử lại.", e);
            }
        }

        // 3) Lưu cập nhật cuối
        try {
            Application saved = applicationRepository.save(app);
            return applicationMapper.toResponse(saved);
        } catch (RuntimeException ex) {
            // Nếu đã upload CV thành công mà DB save lỗi -> dọn asset trên Cloudinary (best effort)
            if (uploadedCv) {
                try { cloudinaryService.deleteApplicationCv(app.getId()); } catch (Exception ignore) {}
            }
            throw ex;
        }
    }

    @Override
    public Page<ApplicationByUserResponse> getByUser(Long userId, Pageable pageable) {
        return applicationRepository.findAllByUser_Id(userId, pageable)
                .map(applicationMapper::toByUserResponse);
    }

    @Override
    public Page<ApplicationResponse> getByJob(Integer jobId, Pageable pageable) {
        return null;
    }
}
