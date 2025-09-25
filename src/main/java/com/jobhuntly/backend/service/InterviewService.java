package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.CreateInterviewRequest;
import com.jobhuntly.backend.dto.response.CandidateInterviewResponse;
import com.jobhuntly.backend.dto.response.InterviewMetaDto;
import com.jobhuntly.backend.dto.response.RecruiterInterviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InterviewService {
    RecruiterInterviewResponse create(CreateInterviewRequest req) throws Exception;

    Page<RecruiterInterviewResponse> listForCompany(Long companyId, Pageable pageable);

    Page<CandidateInterviewResponse> listForCandidate(Long candidateId, Pageable pageable);

    // candidate -> CandidateInterviewResponse; recruiter/admin ->
    // RecruiterInterviewResponse
    Object updateStatus(Long interviewId, String newStatus);

    void sendReminder(Long interviewId);

    void autoComplete(Long interviewId);
    
    InterviewMetaDto getMeta(Long interviewId);
}
