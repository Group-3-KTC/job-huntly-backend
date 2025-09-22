package com.jobhuntly.backend.service.queue;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class InterviewQueues {

    private final RedissonClient redisson;

    private RDelayedQueue<Long> delayed(String name) {
        RQueue<Long> dest = redisson.getQueue(name);
        return redisson.getDelayedQueue(dest);
    }

    public void scheduleReminder(Long interviewId, Instant executeAt) {
        long delayMs = Math.max(0, executeAt.toEpochMilli() - System.currentTimeMillis());
        delayed("q:interview:reminder").offer(interviewId, delayMs, TimeUnit.MILLISECONDS);
    }

    public void scheduleAutoComplete(Long interviewId, Instant executeAt) {
        long delayMs = Math.max(0, executeAt.toEpochMilli() - System.currentTimeMillis());
        delayed("q:interview:auto-complete").offer(interviewId, delayMs, TimeUnit.MILLISECONDS);
    }
}
