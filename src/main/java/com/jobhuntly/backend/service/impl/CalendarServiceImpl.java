package com.jobhuntly.backend.service.impl;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.jobhuntly.backend.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

        private final Calendar calendar;

        @Value("${gcal.calendarId}")
        private String calendarId;

        // (Giữ nguyên) createInterviewEvent cũ nếu bạn còn dùng
        @Override
        public CreatedEvent createInterviewEvent(
                        String title,
                        String description,
                        String recruiterEmail,
                        String candidateEmail,
                        ZonedDateTime startAt,
                        int durationMinutes) throws IOException {
                throw new UnsupportedOperationException(
                                "Use createInterviewEventWithFixedUrl instead for stable Jitsi URL");
        }

        // ✅ Method mới: dùng URL cố định do caller truyền vào
        @Override
        public CreatedEvent createInterviewEventWithFixedUrl(
                        String title,
                        String description,
                        String recruiterEmail,
                        String candidateEmail,
                        ZonedDateTime startAt,
                        int durationMinutes,
                        String meetingUrl) throws IOException {

                DateTime start = new DateTime(startAt.toInstant().toEpochMilli());
                DateTime end = new DateTime(startAt.plusMinutes(durationMinutes).toInstant().toEpochMilli());

                Event event = new Event()
                                .setSummary(title)
                                .setDescription(((description == null || description.isBlank()) ? ""
                                                : (description + "\n\n"))
                                                + "Join interview: " + meetingUrl)
                                .setLocation(meetingUrl) // Link hiện ngay dưới tiêu đề trong Google Calendar
                                .setStart(new EventDateTime().setDateTime(start).setTimeZone(startAt.getZone().getId()))
                                .setEnd(new EventDateTime().setDateTime(end).setTimeZone(startAt.getZone().getId()))
                                // .setAttendees(List.of(
                                //                 new EventAttendee().setEmail(recruiterEmail),
                                //                 new EventAttendee().setEmail(candidateEmail)))
                                .setReminders(new Event.Reminders()
                                                .setUseDefault(false)
                                                .setOverrides(List.of(
                                                                new EventReminder().setMethod("popup").setMinutes(30)
                                                                // new EventReminder().setMethod("email")
                                                                //                 .setMinutes(60)
                                                                                )
                                                                                )
                                                                                );

                Event created = calendar.events()
                                .insert(calendarId, event)
                                .setSendUpdates("none") // gửi mail mời cho attendees
                                .execute();

                return new CreatedEvent(created.getId(), meetingUrl);
        }
}
