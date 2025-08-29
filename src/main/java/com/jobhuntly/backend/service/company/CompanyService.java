package com.jobhuntly.backend.service.company;

import com.jobhuntly.backend.dto.response.CompanyDto;
import com.jobhuntly.backend.dto.response.LocationCompanyResponse;
import com.jobhuntly.backend.entity.Company;

import java.util.List;

public interface CompanyService {
    List<CompanyDto> getAllCompanies();

    CompanyDto getCompanyById(Long id);

    CompanyDto createCompany(CompanyDto companyDto);

    CompanyDto updateCompanyById(Long id, CompanyDto companyDto);

    void deleteCompanyById(Long id);

    List<CompanyDto> getCompaniesByCategories(List<Long> categoryIds);

    List<LocationCompanyResponse> getAllDistinctLocations();

    List<CompanyDto> getCompaniesByLocation(String location);

    List<CompanyDto> getCompaniesByName(String name);

    List<CompanyDto> getCompaniesByNameOrCategory(String name, List<Long> categoryIds);
}
