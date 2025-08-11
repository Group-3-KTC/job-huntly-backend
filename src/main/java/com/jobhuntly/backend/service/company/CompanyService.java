package com.jobhuntly.backend.service.company;

import com.jobhuntly.backend.dto.company.CompanyDto;
import com.jobhuntly.backend.entity.Company;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CompanyService {
    List<CompanyDto> getAllCompanies();

    CompanyDto getCompanyById(Long id);

    CompanyDto createCompany(CompanyDto companyDto);

    CompanyDto updateCompany(Long id, CompanyDto companyDto);

    void deleteCompany(Long id);
}
