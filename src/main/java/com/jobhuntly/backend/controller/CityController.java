package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.CityRequest;
import com.jobhuntly.backend.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/city")
public class CityController {
    private final CityService cityService;

    @GetMapping
    public List<CityRequest> getAllCity() {
        return cityService.getAllCity();
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchCities(@RequestParam String keyword) {
        try {
            List<CityRequest> cities = cityService.getCityByName(keyword);
            return ResponseEntity.ok(cities);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
