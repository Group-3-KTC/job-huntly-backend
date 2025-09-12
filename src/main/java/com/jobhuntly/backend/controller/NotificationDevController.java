package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.service.impl.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${backend.prefix}/dev")
@RequiredArgsConstructor
public class NotificationDevController {
    private final NotificationService notificationService;

//    @PostMapping("/ping-noti")
//    public void ping() {
//        Long userId = SecurityUtils.getCurrentUserId();
//        notificationService.notifyApplicationStatus(userId, 0L, 0L, 0L, "TESTING");
//    }

    @PostMapping("/ping-noti")
    public void pingNoAuth(@RequestParam Long userId,
                           @RequestParam(defaultValue="TESTING") String status) {
        notificationService.notifyApplicationStatus(userId, null, null, null,null, status);
    }

}
