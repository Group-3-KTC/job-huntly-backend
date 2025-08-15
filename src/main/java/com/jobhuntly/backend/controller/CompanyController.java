package com.jobhuntly.backend.controller;

import com.jobhuntly.backend.dto.response.CompanyDto;
import com.jobhuntly.backend.service.company.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("${api.prefix}/companies")
public class CompanyController {
    @Autowired
    private CompanyService companyService;

    // Get List of Companies
    @GetMapping("")
    public ResponseEntity<List<CompanyDto>> getListCompanies() {
        List<CompanyDto> companies = companyService.getAllCompanies();
        if (companies.isEmpty()) {
            return new ResponseEntity<>(companies, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(companies, HttpStatus.OK);
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

    // Create Company
    @PostMapping("/add")
    public ResponseEntity<CompanyDto> createCompany(@RequestBody CompanyDto companyDto) {
        CompanyDto createdCompany = companyService.createCompany(companyDto);
        if (createdCompany == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(createdCompany, HttpStatus.CREATED);
    }
}
