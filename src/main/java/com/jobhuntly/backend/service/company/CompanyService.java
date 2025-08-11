package com.jobhuntly.backend.service.company;

import com.jobhuntly.backend.dto.response.CompanyDto;

import java.util.List;

public interface CompanyService {
    List<CompanyDto> getAllCompanies();

    CompanyDto getCompanyById(Long id);

    CompanyDto createCompany(CompanyDto companyDto);

    CompanyDto updateCompanyById(Long id, CompanyDto companyDto);

    void deleteCompanyById(Long id);
}
