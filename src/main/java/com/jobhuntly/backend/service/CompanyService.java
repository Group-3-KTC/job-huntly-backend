package com.jobhuntly.backend.service;

import com.jobhuntly.backend.dto.request.CompanyRequest;
import com.jobhuntly.backend.dto.response.CompanyDto;
import com.jobhuntly.backend.dto.response.LocationCompanyResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CompanyService {
    // Không phân trang
    List<CompanyDto> getAllCompanies();
    List<CompanyDto> getCompaniesByCategories(List<Long> categoryIds);
    List<CompanyDto> getCompaniesByLocation(String location);
    List<CompanyDto> getCompaniesByName(String name);
    List<CompanyDto> getCompaniesByNameOrCategory(String name, List<Long> categoryIds);
    List<LocationCompanyResponse> getAllDistinctLocations();
    
    // Có phân trang
    Page<CompanyDto> getAllCompaniesWithPagination(int page, int size, String[] sort);
    Page<CompanyDto> getCompaniesByCategoriesWithPagination(List<Long> categoryIds, int page, int size, String[] sort);
    Page<CompanyDto> getCompaniesByLocationWithPagination(String location, int page, int size, String[] sort);
    Page<CompanyDto> getCompaniesByNameWithPagination(String name, int page, int size, String[] sort);
    Page<CompanyDto> getCompaniesByNameOrCategoryWithPagination(String name, List<Long> categoryIds, int page, int size, String[] sort);
    
    // CRUD
    CompanyDto getCompanyById(Long id);
    CompanyDto getCompanyByUserId(Long userId);
    
    CompanyDto createCompany(CompanyRequest companyRequest);
    
    CompanyDto updateCompanyById(Long id, CompanyDto companyDto);
    void deleteCompanyById(Long id);
}
