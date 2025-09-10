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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@AllArgsConstructor
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

    private static final int MAX_ATTEMPTS = 4;                 // gồm cả lần apply đầu tiên
    private static final Duration COOLDOWN = Duration.ofMinutes(10); // chống spam

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
        Page<Application> page = applicationRepository.findAllByJob_Id(jobId.longValue(), pageable);
        return page.map(app -> {
            String viewUrl = app.getCv();
            String downloadUrl = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/api/v1/applications/{id}/cv/download")
                    .buildAndExpand(app.getId())
                    .toUriString();

            return new ApplicationResponse(
                    app.getId(),
                    app.getUser().getId(),
                    app.getJob().getId(),
                    viewUrl,
                    app.getEmail(),
                    app.getStatus(),
                    app.getPhoneNumber(),
                    app.getCandidateName(),
                    app.getCreatedAt(),
                    downloadUrl
            );
        });
    }


    @Override
    public ApplicationResponse update(Long userId, Long jobId, ApplicationRequest req) {
        // khóa ghi để chống double-click hoặc 2 tab cùng gửi
        Application app = applicationRepository.lockByUserAndJob(userId, jobId)
                .orElseThrow(() -> new EntityNotFoundException("Bạn chưa ứng tuyển job này."));

        if (!Objects.equals(app.getUser().getId(), userId)) {
            throw new IllegalStateException("Không có quyền cập nhật hồ sơ ứng tuyển này.");
        }

        // 1) Giới hạn tổng số lần (kể cả lần đầu)
        if (app.getAttemptCount() >= MAX_ATTEMPTS) {
            throw new IllegalStateException("Bạn đã đạt giới hạn 4 lần ứng tuyển cho công việc này.");
        }

        // 2) Cooldown chống spam
        if (app.getLastUserActionAt() != null &&
                app.getLastUserActionAt().isAfter(LocalDateTime.now().minus(COOLDOWN))) {
            throw new IllegalStateException("Bạn vừa cập nhật hồ sơ, vui lòng thử lại sau.");
        }

        // 3) Cập nhật thông tin hồ sơ (KHÔNG thay đổi status — company mới có quyền đổi)
        if (req.getEmail() != null)         app.setEmail(req.getEmail());
        if (req.getPhoneNumber() != null)   app.setPhoneNumber(req.getPhoneNumber());
        if (req.getCandidateName() != null) app.setCandidateName(req.getCandidateName());
        if (req.getDescription() != null)   app.setDescription(req.getDescription());

        // 4) Upload CV nếu có
        MultipartFile cvFile = req.getCvFile();
        boolean uploadedCv = false;
        if (cvFile != null && !cvFile.isEmpty()) {
            try {
                var asset = cloudinaryService.uploadApplicationCv(app.getId(), cvFile);
                app.setCv(asset.secureUrl());
                uploadedCv = true;
            } catch (IOException e) {
                throw new IllegalStateException("Upload CV thất bại. Vui lòng thử lại.", e);
            }
        }

        // 5) Tăng attempt + stamp thời gian user action và lưu DB
        try {
            app.setAttemptCount(app.getAttemptCount() + 1);
            app.setLastUserActionAt(LocalDateTime.now());

            Application saved = applicationRepository.save(app);
            return applicationMapper.toResponse(saved);
        } catch (RuntimeException ex) {
            if (uploadedCv) {
                try { cloudinaryService.deleteApplicationCv(app.getId()); } catch (Exception ignore) {}
            }
            throw ex;
        }
    }

    @Override
    public ApplicationResponse getDetail(Long userId, Integer jobId) {
        Application app = applicationRepository.findByUser_IdAndJob_Id(userId, jobId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hồ sơ ứng tuyển."));
        return applicationMapper.toResponse(app);
    }

    @Override
    public boolean hasApplied(Long userId, Long jobId) {
        return applicationRepository.existsByUser_IdAndJob_Id(userId, jobId);
    }
}
