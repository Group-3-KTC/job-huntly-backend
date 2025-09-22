package com.jobhuntly.backend.service;

import java.io.IOException;
import java.time.ZonedDateTime;

public interface CalendarService {

    // (Tuỳ bạn giữ hoặc xoá) Method cũ
    CreatedEvent createInterviewEvent(
            String title,
            String description,
            String recruiterEmail,
            String candidateEmail,
            ZonedDateTime startAt,
            int durationMinutes) throws IOException;

    // ✅ Method mới: tạo event với URL đã biết (ổn định theo interviewId)
    CreatedEvent createInterviewEventWithFixedUrl(
            String title,
            String description,
            String recruiterEmail,
            String candidateEmail,
            ZonedDateTime startAt,
            int durationMinutes,
            String meetingUrl) throws IOException;

    // Để tiện dùng chung
    record CreatedEvent(String eventId, String meetingUrl) {
    }
}
