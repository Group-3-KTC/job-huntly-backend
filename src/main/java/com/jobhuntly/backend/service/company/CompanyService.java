package com.jobhuntly.backend.service.company;

import com.jobhuntly.backend.dto.company.CompanyDto;
import com.jobhuntly.backend.entity.Company;

import java.util.List;

public interface CompanyService {
    List<CompanyDto> getAllCompanies();
    CompanyDto getCompanyById(Long id);

}
