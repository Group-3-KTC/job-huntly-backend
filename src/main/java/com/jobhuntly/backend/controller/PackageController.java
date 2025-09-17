package com.jobhuntly.backend.controller;


import com.jobhuntly.backend.dto.request.PackageRequest;
import com.jobhuntly.backend.dto.response.PackageResponse;
import com.jobhuntly.backend.service.PackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${backend.prefix}/packages")
public class PackageController {
    private final PackageService packageService;

    @GetMapping
    public List<PackageResponse> getAll() {
        return packageService.getAll();
    }

    @GetMapping("/{id}")
    public PackageResponse getById(@PathVariable Long id) {
        return packageService.getById(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PackageResponse> create(@Valid @RequestBody PackageRequest req) {
        PackageResponse created = packageService.create(req);

        var location = ServletUriComponentsBuilder
                .fromCurrentRequest()        // /api/v1/packages
                .path("/{id}")               // -> /api/v1/packages/{id}
                .buildAndExpand(created.getPackageId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    // PUT: cập nhật đầy đủ hoặc 1 phần (ở đây xử lý như PATCH: null bị bỏ qua)
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PackageResponse update(@PathVariable Long id, @Valid @RequestBody PackageRequest req) {
        return packageService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        packageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
