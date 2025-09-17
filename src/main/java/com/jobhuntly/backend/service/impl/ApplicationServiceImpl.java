package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.ApplicationRequest;
import com.jobhuntly.backend.dto.response.ApplicationByUserResponse;
import com.jobhuntly.backend.dto.response.ApplicationResponse;
import com.jobhuntly.backend.dto.response.ApplyStatusResponse;
import com.jobhuntly.backend.entity.Application;
import com.jobhuntly.backend.entity.Job;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.mapper.ApplicationMapper;
import com.jobhuntly.backend.repository.ApplicationRepository;
import com.jobhuntly.backend.repository.CompanyRepository;
import com.jobhuntly.backend.repository.JobRepository;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.service.ApplicationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
@AllArgsConstructor
@Transactional
public class ApplicationServiceImpl implements ApplicationService {

	private static final int MAX_ATTEMPTS = 2;                 // gồm cả lần apply đầu tiên
	private static final Duration COOLDOWN = Duration.ofMinutes(30); // chống spam

	private final ApplicationRepository applicationRepository;
	private final UserRepository userRepository;
	private final JobRepository jobRepository;
	private final ApplicationMapper applicationMapper;
	private final CloudinaryService cloudinaryService;
	private final NotificationService notificationService;
	private final CompanyRepository companyRepository;
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
		applicationRepository.saveAndFlush(app);

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
					app.getDescription(),
					app.getCandidateName(),
					app.getCreatedAt(),
					downloadUrl
			);
		});
	}

	// Lấy applications theo companyId với kiểm tra quyền (ADMIN hoặc chủ công ty)
	@Override
	public Page<ApplicationResponse> getByCompany(Long requesterUserId, Long companyId, Pageable pageable) {
		User requester = userRepository.findById(requesterUserId)
				.orElseThrow(() -> new EntityNotFoundException("User không tồn tại"));

		String roleName = requester.getRole() != null ? requester.getRole().getRoleName() : null;
		boolean isAdmin = roleName != null && "ADMIN".equalsIgnoreCase(roleName);

		Long ownerUserId = companyRepository.findById(companyId)
				.map(c -> c.getUser().getId())
				.orElseThrow(() -> new EntityNotFoundException("Company không tồn tại"));

		boolean isOwner = Objects.equals(ownerUserId, requesterUserId);
		if (!isAdmin && !isOwner) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền xem danh sách ứng tuyển của công ty này");
		}

		Page<Application> page = applicationRepository.findAllByJob_Company_Id(companyId, pageable);
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
					app.getDescription(),
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
			throw new IllegalStateException(
					"Bạn đã đạt giới hạn " + MAX_ATTEMPTS + " lần ứng tuyển cho công việc này."
			);
		}

		// 2) Cooldown chống spam
		if (app.getLastUserActionAt() != null &&
				app.getLastUserActionAt().isAfter(LocalDateTime.now().minus(COOLDOWN))) {

			LocalDateTime now = LocalDateTime.now();
			LocalDateTime deadline = app.getLastUserActionAt().plus(COOLDOWN);
			long minutesLeft = Duration.between(now, deadline).toMinutes();

			throw new IllegalStateException(
					"Bạn vừa cập nhật hồ sơ cách đây chưa lâu. "
						+ "Vui lòng thử lại sau khoảng " + minutesLeft + " phút nữa."
			);
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
	public ApplicationResponse getDetail(Long userId, Long jobId) {
		Application app = applicationRepository.findByUser_IdAndJob_Id(userId, jobId)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hồ sơ ứng tuyển."));
		return applicationMapper.toResponse(app);
	}

	@Override
	public ApplyStatusResponse hasApplied(Long userId, Long jobId) {
		return applicationRepository.findByUser_IdAndJob_Id(userId, jobId)
				.map(app -> new ApplyStatusResponse(
						true,
						app.getAttemptCount(),
						app.getLastUserActionAt()
				))
				.orElse(new ApplyStatusResponse(false, null, null));

	}

	@Override
	public ApplicationResponse updateStatusByStaff(Long userId, Long applicationId, String status) {
		Application app = applicationRepository.lockById(applicationId)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hồ sơ ứng tuyển."));

		// Chỉ cập nhật status, không tăng attempt, không stamp
		app.setStatus(status);

		Application saved = applicationRepository.save(app);
		try {
			notificationService.notifyApplicationStatus(
					saved.getUser().getId(),
					saved.getJob().getId(),
					saved.getJob().getCompany().getId(),
					saved.getId().longValue(),
					saved.getJob().getCompany().getCompanyName(),
					status
			);
		} catch (Exception ignore) {
			// không làm hỏng flow cập nhật nếu gửi noti lỗi
		}
		return applicationMapper.toResponse(saved);
	}

    @Override
    public Set<Long> findAppliedJobIds(Long userId, Collection<Long> jobIds) {
        if (userId == null || jobIds == null || jobIds.isEmpty()) return Set.of();
        return new HashSet<>(applicationRepository.findAppliedJobIdsIn(userId, jobIds));
    }
}
