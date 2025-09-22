package com.jobhuntly.backend.worker;

import com.jobhuntly.backend.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.Executors;

@Component
@ConditionalOnBean(RedissonClient.class)
@RequiredArgsConstructor
@Slf4j
public class InterviewWorkers {

    private final RedissonClient redisson;
    private final InterviewService interviewService;

    @PostConstruct
    void start() {
        // Worker: reminder
        Executors.newSingleThreadExecutor(r -> new Thread(r, "interview-reminder-worker")).submit(() -> {
            RBlockingQueue<Long> bq = redisson.getBlockingQueue("q:interview:reminder"); // <-- dùng blocking queue
            while (true) {
                Long id = bq.take(); // block đợi item (từ delayed queue chuyển qua)
                try {
                    interviewService.sendReminder(id);
                } catch (Exception e) {
                    log.error("sendReminder failed for {}", id, e);
                }
            }
        });

        // Worker: auto complete
        Executors.newSingleThreadExecutor(r -> new Thread(r, "interview-complete-worker")).submit(() -> {
            RBlockingQueue<Long> bq = redisson.getBlockingQueue("q:interview:auto-complete"); // <-- dùng blocking queue
            while (true) {
                Long id = bq.take();
                try {
                    interviewService.autoComplete(id);
                } catch (Exception e) {
                    log.error("autoComplete failed for {}", id, e);
                }
            }
        });
    }
}
