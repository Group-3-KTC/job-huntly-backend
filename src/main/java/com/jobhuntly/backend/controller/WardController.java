package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.service.WardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${backend.prefix}/wards")
@RequiredArgsConstructor
public class WardController {
    private final WardService wardService;

    @GetMapping
    public ResponseEntity<?> getWardsByExactCity(@RequestParam String cityName) {
        try {
            return ResponseEntity.ok(wardService.getWardByCityName(cityName));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
