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

        Application app = applicationMapper.toEntity(req);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User không tồn tại"));
        Job job = jobRepository.findById(req.getJobId())
                .orElseThrow(() -> new EntityNotFoundException("Job không tồn tại"));
        app.setUser(user);
        app.setJob(job);

        // Upload CV nếu có
        MultipartFile cvFile = req.getCvFile();
        if (cvFile != null && !cvFile.isEmpty()) {
            try {
                String secureUrl = cloudinaryService.uploadFile(cvFile);
                app.setCv(secureUrl); // lưu URL vào DB
            } catch (Exception e) {
                throw new IllegalStateException("Upload CV thất bại. Vui lòng thử lại.", e);
            }
        }

        // Để @PrePersist tự set "Applied"
        app.setStatus(null);

        Application saved = applicationRepository.save(app);
        return applicationMapper.toResponse(saved);
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
