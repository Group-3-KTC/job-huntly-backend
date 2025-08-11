package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.response.CompanyDto;
import com.jobhuntly.backend.service.company.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/companies")
public class CompanyController {
    @Autowired
    private CompanyService companyService;

    @GetMapping("")
    public ResponseEntity<List<CompanyDto>> getListCompanies() {
        List<CompanyDto> companies = companyService.getAllCompanies();
        if (companies.isEmpty()) {
            return new ResponseEntity<>(companies, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(companies, HttpStatus.OK);
    }
}
