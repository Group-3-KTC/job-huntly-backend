package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.request.CreateInterviewRequest;
import com.jobhuntly.backend.dto.response.CandidateInterviewResponse;
import com.jobhuntly.backend.dto.response.RecruiterInterviewResponse;
import com.jobhuntly.backend.entity.Interview;
import com.jobhuntly.backend.entity.Job;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.repository.CompanyRepository;
import com.jobhuntly.backend.repository.InterviewRepository;
import com.jobhuntly.backend.repository.JobRepository;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.service.CalendarService;
import com.jobhuntly.backend.service.InterviewService;
import com.jobhuntly.backend.service.MailService;
import com.jobhuntly.backend.service.queue.InterviewQueues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewServiceImpl implements InterviewService {

    private final InterviewRepository interviewRepository;
    private final CalendarService calendarService;
    private final InterviewQueues queues;

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final MailService mailService;

    /**
     * Email recruiter = email của user đang thao tác nếu là RECRUITER; nếu ADMIN
     * thì dùng owner của company
     */
    private String resolveRecruiterEmail(Long companyId) {
        Long meId = SecurityUtils.getCurrentUserId();
        var me = userRepository.findById(meId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        String role = me.getRole() != null ? me.getRole().getRoleName() : null;
        if ("RECRUITER".equalsIgnoreCase(role)) {
            return Optional.ofNullable(me.getEmail()).orElse("contact.jobhuntly@gmail.com");
        }

        Long ownerId = companyRepository.findOwnerUserIdById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
        return userRepository.findById(ownerId)
                .map(u -> Optional.ofNullable(u.getEmail()).orElse("contact.jobhuntly@gmail.com"))
                .orElse("contact.jobhuntly@gmail.com");
    }

    private String resolveCandidateEmail(Long candidateUserId) {
        return userRepository.findById(candidateUserId)
                .map(u -> Optional.ofNullable(u.getEmail()).orElse("candidate@example.com"))
                .orElse("candidate@example.com");
    }

    /** DTO cho recruiter view (không còn application) */
    private RecruiterInterviewResponse toRecruiterView(Interview iv, Job job, User candidate) {
        return new RecruiterInterviewResponse(
                iv.getInterviewId(),
                iv.getScheduledAt(),
                iv.getDurationMinutes(),
                iv.getStatus().name(),
                iv.getMeetingUrl(),
                job.getId(),
                job.getTitle(),
                candidate.getId(),
                candidate.getFullName(),
                candidate.getEmail());
    }

    /** DTO cho candidate view (không còn application) */
    private CandidateInterviewResponse toCandidateView(Interview iv, Job job) {
        return new CandidateInterviewResponse(
                iv.getInterviewId(),
                iv.getScheduledAt(),
                iv.getDurationMinutes(),
                iv.getStatus().name(),
                iv.getMeetingUrl(),
                job.getId(),
                job.getTitle(),
                job.getCompany().getId(),
                job.getCompany().getCompanyName());
    }

    @Transactional
    @Override
    public RecruiterInterviewResponse create(CreateInterviewRequest req) throws Exception {
        Long jobId = req.jobId();
        Long companyId = req.companyId();
        Long candidateId = req.candidateId();

        LocalDateTime when = req.scheduledAt();
        if (when == null || !when.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scheduledAt must be in the future");
        }
        int dur = (req.durationMinutes() == null || req.durationMinutes() < 15) ? 60 : req.durationMinutes();

        Long meId = SecurityUtils.getCurrentUserId();
        var me = userRepository.findById(meId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        String role = me.getRole() != null ? me.getRole().getRoleName() : null;
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        boolean isRecruiter = "RECRUITER".equalsIgnoreCase(role);
        if (!isAdmin && !isRecruiter) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only recruiter or admin can create interviews");
        }

        Long ownerId = companyRepository.findOwnerUserIdById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
        if (isRecruiter && !Objects.equals(ownerId, meId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not owner of this company");
        }

        Long jobCompanyId = jobRepository.findCompanyIdByJobId(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        if (!Objects.equals(jobCompanyId, companyId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job does not belong to the provided company");
        }

        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
        String candRole = candidate.getRole() != null ? candidate.getRole().getRoleName() : null;
        if (!"CANDIDATE".equalsIgnoreCase(candRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "candidateId must be a CANDIDATE user");
        }

        String recruiterEmail = resolveRecruiterEmail(companyId);
        String candidateEmail = resolveCandidateEmail(candidateId);

        // 1) Lưu trước để có interviewId
        Interview iv = new Interview();
        iv.setJobId(jobId);
        iv.setCompanyId(companyId);
        iv.setCandidateId(candidateId);
        iv.setScheduledAt(when);
        iv.setDurationMinutes(dur);
        iv.setStatus(Interview.Status.PENDING);
        interviewRepository.save(iv);

        // 2) URL Jitsi ổn định theo interviewId
        String vcUrlFixed = "https://meet.jit.si/jobhuntly-" + iv.getInterviewId();

        // 3) Tạo event Calendar (không attendees khi dùng SA không DWD)
        ZonedDateTime startAt = when.atZone(ZoneId.systemDefault());
        var created = calendarService.createInterviewEventWithFixedUrl(
                "Interview: Job#" + jobId,
                "Auto-created by JobHuntly",
                recruiterEmail, candidateEmail,
                startAt, dur,
                vcUrlFixed);

        // 4) Cập nhật meetingUrl + gcalEventId
        iv.setMeetingUrl(created.meetingUrl());
        iv.setGcalEventId(created.eventId());
        interviewRepository.save(iv);

        // 5) Queue reminder + auto-complete
        queues.scheduleReminder(iv.getInterviewId(), when.minusMinutes(5).atZone(ZoneId.systemDefault()).toInstant());
        queues.scheduleAutoComplete(iv.getInterviewId(),
                when.plusMinutes(dur).atZone(ZoneId.systemDefault()).toInstant());

        // 6) Email mời (kèm .ics) cho recruiter & candidate
        try {
            mailService.sendInterviewCreated(recruiterEmail, candidateEmail, iv);
        } catch (Exception ex) {
            log.warn("sendInterviewCreated failed", ex);
        }

        Job job = jobRepository.findById(jobId).orElseThrow();
        return toRecruiterView(iv, job, candidate);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RecruiterInterviewResponse> listForCompany(Long companyId, Pageable pageable) {
        Long meId = SecurityUtils.getCurrentUserId();
        var me = userRepository.findById(meId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        String role = me.getRole() != null ? me.getRole().getRoleName() : null;
        boolean isRecruiter = "RECRUITER".equalsIgnoreCase(role);

        if (isRecruiter) {
            Long ownerId = companyRepository.findOwnerUserIdById(companyId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
            if (!Objects.equals(ownerId, meId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not owner of this company");
            }
        }

        return interviewRepository.findByCompanyIdOrderByScheduledAtDesc(companyId, pageable)
                .map(iv -> {
                    Job job = jobRepository.findById(iv.getJobId()).orElseThrow();
                    User candidate = userRepository.findById(iv.getCandidateId()).orElseThrow();
                    return toRecruiterView(iv, job, candidate);
                });
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CandidateInterviewResponse> listForCandidate(Long candidateId, Pageable pageable) {
        Long meId = SecurityUtils.getCurrentUserId();
        var me = userRepository.findById(meId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        String role = me.getRole() != null ? me.getRole().getRoleName() : null;
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        if (!isAdmin && !Objects.equals(meId, candidateId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot view other candidate's interviews");
        }

        return interviewRepository.findByCandidateIdOrderByScheduledAtDesc(candidateId, pageable)
                .map(iv -> {
                    Job job = jobRepository.findById(iv.getJobId()).orElseThrow();
                    return toCandidateView(iv, job);
                });
    }

    @Transactional
    @Override
    public Object updateStatus(Long interviewId, String newStatus) {
        Interview iv = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found"));

        Interview.Status s;
        try {
            s = Interview.Status.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
        }

        Long meId = SecurityUtils.getCurrentUserId();
        var me = userRepository.findById(meId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        String role = me.getRole() != null ? me.getRole().getRoleName() : null;

        boolean isRecruiter = "RECRUITER".equalsIgnoreCase(role);
        boolean isCandidate = "CANDIDATE".equalsIgnoreCase(role);

        if (isCandidate) {
            if (!Objects.equals(meId, iv.getCandidateId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your interview");
            }
            if (!(s == Interview.Status.ACCEPTED || s == Interview.Status.DECLINED)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Candidate can only set ACCEPTED or DECLINED");
            }
        } else if (isRecruiter) {
            Long ownerId = companyRepository.findOwnerUserIdById(iv.getCompanyId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
            if (!Objects.equals(ownerId, meId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not owner of this company");
            }
            if (!(s == Interview.Status.COMPLETED || s == Interview.Status.CANCELLED)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Recruiter can only set COMPLETED or CANCELLED");
            }
        } // Admin: full

        iv.setStatus(s);
        interviewRepository.save(iv);

        if ("ACCEPTED".equalsIgnoreCase(newStatus) || "DECLINED".equalsIgnoreCase(newStatus)) {
            try {
                String recruiterEmail = resolveRecruiterEmail(iv.getCompanyId());
                mailService.sendInterviewStatusChangedToRecruiter(recruiterEmail, iv, newStatus);
            } catch (Exception ex) {
                log.warn("sendInterviewStatusChangedToRecruiter failed", ex);
            }
        }

        Job job = jobRepository.findById(iv.getJobId()).orElseThrow();
        if (isCandidate && Objects.equals(meId, iv.getCandidateId())) {
            return toCandidateView(iv, job);
        } else {
            User candidate = userRepository.findById(iv.getCandidateId()).orElseThrow();
            return toRecruiterView(iv, job, candidate);
        }
    }

    @Transactional
    @Override
    public void sendReminder(Long interviewId) {
        Interview i = interviewRepository.findById(interviewId).orElse(null);
        if (i == null || Boolean.TRUE.equals(i.getReminderSent()))
            return;
        if (i.getStatus() == Interview.Status.CANCELLED || i.getStatus() == Interview.Status.DECLINED)
            return;

        try {
            String recruiterEmail = resolveRecruiterEmail(i.getCompanyId());
            String candidateEmail = resolveCandidateEmail(i.getCandidateId());
            mailService.sendInterviewReminder(recruiterEmail, candidateEmail, i);
        } catch (Exception ex) {
            log.warn("sendInterviewReminder failed", ex);
        }

        i.setReminderSent(true);
        interviewRepository.save(i);
        log.info("Reminder sent for interview {}", interviewId);
    }

    @Transactional
    @Override
    public void autoComplete(Long interviewId) {
        Interview i = interviewRepository.findById(interviewId).orElse(null);
        if (i == null)
            return;
        if (i.getStatus() == Interview.Status.PENDING || i.getStatus() == Interview.Status.ACCEPTED) {
            i.setStatus(Interview.Status.COMPLETED);
            interviewRepository.save(i);
            log.info("Interview {} marked COMPLETED", interviewId);
        }
    }
}
