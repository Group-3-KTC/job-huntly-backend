package com.jobhuntly.backend.service;

import com.jobhuntly.backend.entity.Interview;

public interface MailService {

    void sendInterviewCreated(String recruiterEmail, String candidateEmail, Interview i);

    void sendInterviewReminder(String recruiterEmail, String candidateEmail, Interview i);

    void sendInterviewStatusChangedToRecruiter(String recruiterEmail, Interview i, String newStatus);
    
    void sendInterviewCreatedTo(String toEmail, Interview i, String portalUrl) throws Exception;
}
