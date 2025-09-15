package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.request.CompanyRequest;
import com.jobhuntly.backend.dto.response.CompanyDto;
import com.jobhuntly.backend.dto.response.LocationCompanyResponse;
import com.jobhuntly.backend.security.SecurityUtils;
import com.jobhuntly.backend.service.CompanyService;
import com.jobhuntly.backend.service.impl.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("${backend.prefix}/companies")
public class CompanyController {
    @Autowired
    private CompanyService companyService;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    // Get current user's company
    @GetMapping("/my-company")
    public ResponseEntity<CompanyDto> getMyCompany() {
        Long userId = SecurityUtils.getCurrentUserId();
        CompanyDto company = companyService.getCompanyByUserId(userId);
        if (company == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(company, HttpStatus.OK);
    }

    // Get List of Companies
    @GetMapping("")
    public ResponseEntity<?> getListCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort,
            @RequestParam(defaultValue = "false") boolean unpaged) {
        
        if (unpaged) {
            // Trường hợp đặc biệt: Lấy tất cả (dành cho export/select nhỏ)
            List<CompanyDto> companies = companyService.getAllCompanies();
            if (companies.isEmpty()) {
                return new ResponseEntity<>(companies, HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(companies, HttpStatus.OK);
        }
        
        Page<CompanyDto> companyPage = companyService.getAllCompaniesWithPagination(page, size, sort);
        if (companyPage.isEmpty()) {
            return new ResponseEntity<>(companyPage, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(companyPage, HttpStatus.OK);
    }

    // Get Detail Company by Id
    @GetMapping("/{id}")
    public ResponseEntity<CompanyDto> getCompanyById(@PathVariable("id") Long id) {
        CompanyDto company = companyService.getCompanyById(id);
        if (company == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(company, HttpStatus.OK);
    }

    // Delete Company by Id
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteCompanyById(@PathVariable("id") Long id) {
        companyService.deleteCompanyById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // Update Company by Id
    @PatchMapping("/{id}")
    public ResponseEntity<CompanyDto> updateCompanyById(@PathVariable("id") Long id, @RequestBody CompanyDto companyDto) {
        CompanyDto updatedCompany = companyService.updateCompanyById(id, companyDto);
        if (updatedCompany == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(updatedCompany, HttpStatus.OK);
    }

    // Create Company với ảnh - Sử dụng @ModelAttribute cho gọn
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompanyDto> createCompany(@ModelAttribute CompanyRequest companyRequest) {
        try {
            // Parse categoryIds nếu có
            if (companyRequest.getCategoryIds() != null && !companyRequest.getCategoryIds().isEmpty()) {
                // categoryIds đã được Spring tự động bind từ FormData
                // Không cần parse thêm
            }

            CompanyDto createdCompany = companyService.createCompany(companyRequest);
            return new ResponseEntity<>(createdCompany, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Lấy công ty theo danh sách category (by-categories?categoryIds=1,2,3)
    @GetMapping("/by-categories")
    public ResponseEntity<?> getCompaniesByCategories(
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        
        if (page == null || size == null) {
            List<CompanyDto> companies = companyService.getCompaniesByCategories(categoryIds);
            if (companies.isEmpty()) {
                return new ResponseEntity<>(companies, HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(companies, HttpStatus.OK);
        }
        
        Page<CompanyDto> companyPage = companyService.getCompaniesByCategoriesWithPagination(categoryIds, page, size, sort);
        if (companyPage.isEmpty()) {
            return new ResponseEntity<>(companyPage, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(companyPage, HttpStatus.OK);
    }

    // Lấy danh sách các địa điểm không trùng lặp
    @GetMapping("/locations")
    public ResponseEntity<List<LocationCompanyResponse>> getAllDistinctLocations() {
        List<LocationCompanyResponse> locations = companyService.getAllDistinctLocations();
        if (locations.isEmpty()) {
            return new ResponseEntity<>(locations, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(locations, HttpStatus.OK);
    }

    // Tìm công ty theo địa điểm ( by-location?location=Ho Chi Minh )
    @GetMapping("/by-location")
    public ResponseEntity<?> getCompaniesByLocation(
            @RequestParam String location,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        
        if (page == null || size == null) {
            List<CompanyDto> companies = companyService.getCompaniesByLocation(location);
            if (companies.isEmpty()) {
                return new ResponseEntity<>(companies, HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(companies, HttpStatus.OK);
        }
        
        Page<CompanyDto> companyPage = companyService.getCompaniesByLocationWithPagination(location, page, size, sort);
        if (companyPage.isEmpty()) {
            return new ResponseEntity<>(companyPage, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(companyPage, HttpStatus.OK);
    }

    // Tìm kiếm công ty theo tên
    @GetMapping("/by-name")
    public ResponseEntity<?> searchCompaniesByName(
            @RequestParam String name,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {
        
        if (page == null || size == null) {
            List<CompanyDto> companies = companyService.getCompaniesByName(name);
            if (companies.isEmpty()) {
                return new ResponseEntity<>(companies, HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(companies, HttpStatus.OK);
        }

        Page<CompanyDto> companyPage = companyService.getCompaniesByNameWithPagination(name, page, size, sort);
        if (companyPage.isEmpty()) {
            return new ResponseEntity<>(companyPage, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(companyPage, HttpStatus.OK);
    }

    // Tìm kiếm công ty theo tên hoặc ngành nghề
    @GetMapping("/search")
    public ResponseEntity<?> searchCompanies(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        if (page == null || size == null) {
            List<CompanyDto> companies = companyService.getCompaniesByNameOrCategory(name, categoryIds);
            if (companies.isEmpty()) {
                return new ResponseEntity<>(companies, HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(companies, HttpStatus.OK);
        }

        Page<CompanyDto> companyPage = companyService.getCompaniesByNameOrCategoryWithPagination(name, categoryIds, page, size, sort);
        if (companyPage.isEmpty()) {
            return new ResponseEntity<>(companyPage, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(companyPage, HttpStatus.OK);
    }
}
