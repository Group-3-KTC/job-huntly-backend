package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.response.CompanyDto;
import com.jobhuntly.backend.dto.response.LocationCompanyResponse;
import com.jobhuntly.backend.entity.Category;
import com.jobhuntly.backend.entity.Company;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.exception.ResourceNotFoundException;
import com.jobhuntly.backend.mapper.CompanyMapper;
import com.jobhuntly.backend.repository.CategoryRepository;
import com.jobhuntly.backend.repository.CompanyRepository;
import com.jobhuntly.backend.repository.JobRepository;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.service.company.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyMapper companyMapper;
    private final CategoryRepository categoryRepository;
    private final JobRepository jobRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDto> getAllCompanies() {
        return companyMapper.toDtoList(companyRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDto getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company ID Not found: " + id));
        CompanyDto dto = companyMapper.toDto(company);
        dto.setJobsCount(jobRepository.countJobsByCompanyId(company.getId()));

        // Trả về categoryIds
        if (company.getCategories() != null) {
            dto.setCategoryIds(company.getCategories().stream()
                    .map(Category::getId)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    @Override
    @Transactional
    public CompanyDto createCompany(CompanyDto companyDto) {
        companyDto.setId(null);

        Long userId = companyDto.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("userId là bắt buộc");
        }

        // 1) Kiểm tra recruiter đã có công ty chưa
        if (companyRepository.existsByUser_Id(userId)) {
            throw new IllegalArgumentException("Recruiter này đã có công ty");
        }

        // 2. Load User và kiểm tra role
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!"RECRUITER".equalsIgnoreCase(user.getRole().getRoleName())) {
            throw new IllegalArgumentException("Chỉ tạo công ty mới cho RECRUITER");
        }

        Company entity = companyMapper.toEntity(companyDto);
        entity.setUser(user);

        // set categories từ categoryIds
        if (companyDto.getCategoryIds() != null && !companyDto.getCategoryIds().isEmpty()) {
            Set<Category> cats = new HashSet<>(categoryRepository.findAllById(companyDto.getCategoryIds()));
            entity.setCategories(cats);
        }

        Company saved = companyRepository.save(entity);
        CompanyDto out = companyMapper.toDto(saved);
        if (saved.getCategories() != null) {
            out.setCategoryIds(saved.getCategories().stream()
                    .map(Category::getId)
                    .collect(Collectors.toSet()));
        }
        return out;
    }

    @Override
    @Transactional
    public CompanyDto updateCompanyById(Long id, CompanyDto companyDto) {
        Company existing = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company ID Not found: " + id));

        companyDto.setId(id);
        companyMapper.updateEntityFromDto(companyDto, existing);

        if (companyDto.getUserId() != null
                && !companyDto.getUserId().equals(existing.getUser().getId())) {

            User user = userRepository.findById(companyDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + companyDto.getUserId()));
            if (!"RECRUITER".equalsIgnoreCase(user.getRole().getRoleName())) {
                throw new IllegalArgumentException("Chỉ gán công ty cho RECRUITER");
            }
            // Nếu đã có công ty khác thì chặn (nếu business yêu cầu)
            if (companyRepository.existsByUser_Id(user.getId())
                    && !existing.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Recruiter này đã sở hữu công ty khác");
            }
            existing.setUser(user);
        }

        if (companyDto.getCategoryIds() != null) {
            Set<Category> cats = new java.util.HashSet<>(categoryRepository.findAllById(companyDto.getCategoryIds()));
            existing.setCategories(cats); // replace toàn bộ set
        }

        Company saved = companyRepository.save(existing);
        CompanyDto out = companyMapper.toDto(saved);
        if (saved.getCategories() != null) {
            out.setCategoryIds(saved.getCategories().stream()
                    .map(Category::getId)
                    .collect(java.util.stream.Collectors.toSet()));
        }
        return out;
    }

    @Override
    @Transactional
    public void deleteCompanyById(Long id) {
        companyRepository.findById(id)
                .ifPresentOrElse(
                        companyRepository::delete,
                        () -> {
                            throw new ResourceNotFoundException("Not found: " + id);
                        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDto> getCompaniesByCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return getAllCompanies();
        }

        List<Company> companies = companyRepository.findByCategoryIdsIncludingParents(categoryIds);
        return getCompanyDtos(companies);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationCompanyResponse> getAllDistinctLocations() {
        return companyRepository.findAllDistinctLocations();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDto> getCompaniesByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return getAllCompanies();
        }

        List<Company> companies = companyRepository.findByLocation(location);
        return getCompanyDtos(companies);
    }

    @Override
    public List<CompanyDto> getCompaniesByName(String name) {
        List<Company> companies = companyRepository.findAllByCompanyNameIgnoreCase(name);
        return getCompanyDtos(companies);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDto> getCompaniesByNameOrCategory(String name, List<Long> categoryIds) {
        boolean isCategoryIdsEmpty = categoryIds == null || categoryIds.isEmpty();
        List<Long> safeList = categoryIds == null ? java.util.Collections.emptyList() : categoryIds;

        List<Company> companies = companyRepository.searchCompanies(
                name == null || name.trim().isEmpty() ? null : name.trim(),
                safeList,
                isCategoryIdsEmpty);

        return getCompanyDtos(companies);
    }

    private List<CompanyDto> getCompanyDtos(List<Company> companies) {
        List<CompanyDto> dtos = companyMapper.toDtoList(companies);

        for (int i = 0; i < companies.size(); i++) {
            Company company = companies.get(i);
            CompanyDto dto = dtos.get(i);

            dto.setJobsCount(jobRepository.countJobsByCompanyId(company.getId()));

            if (company.getCategories() != null) {
                dto.setCategoryIds(company.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet()));
            }
        }

        return dtos;
    }

    public Page<CompanyDto> getAllCompaniesWithPagination(int page, int size, String[] sort) {
        Pageable pageable = createPageableFromParams(page, size, sort);
        Page<Company> companyPage = companyRepository.findAll(pageable);
        return companyPage.map(companyMapper::toDto);
    }

    @Override
    public Page<CompanyDto> getCompaniesByCategoriesWithPagination(List<Long> categoryIds, int page, int size, String[] sort) {
        // Xử lý sort
        Sort sortable = createSortFromParams(sort);
        Pageable pageable = PageRequest.of(page, size, sortable);

        if (categoryIds == null || categoryIds.isEmpty()) {
            return getAllCompaniesWithPagination(page, size, sort);
        }

        Page<Company> companiesPage = companyRepository.findByCategoryIdsIncludingParents(categoryIds, pageable);
        return companiesPage.map(companyMapper::toDto);
    }

    @Override
    public Page<CompanyDto> getCompaniesByLocationWithPagination(String location, int page, int size, String[] sort) {
        Sort sortable = createSortFromParams(sort);
        Pageable pageable = PageRequest.of(page, size, sortable);

        Page<Company> companiesPage = companyRepository.findByLocation(location, pageable);
        return companiesPage.map(companyMapper::toDto);
    }

    @Override
    public Page<CompanyDto> getCompaniesByNameWithPagination(String name, int page, int size, String[] sort) {
        Sort sortable = createSortFromParams(sort);
        Pageable pageable = PageRequest.of(page, size, sortable);

        Page<Company> companiesPage = companyRepository.findAllByCompanyNameIgnoreCase(name, pageable);
        return companiesPage.map(companyMapper::toDto);
    }

    @Override
    public Page<CompanyDto> getCompaniesByNameOrCategoryWithPagination(String name, List<Long> categoryIds, int page, int size, String[] sort) {
        Sort sortable = createSortFromParams(sort);
        Pageable pageable = PageRequest.of(page, size, sortable);

        boolean categoryIdsEmpty = categoryIds == null || categoryIds.isEmpty();
        Page<Company> companiesPage = companyRepository.searchCompanies(name, categoryIds, categoryIdsEmpty, pageable);
        return companiesPage.map(companyMapper::toDto);
    }

    // Phương thức tiện ích để tạo Pageable từ các tham số
    private Pageable createPageableFromParams(int page, int size, String[] sort) {
        String sortField = "id";
        Sort.Direction direction = Sort.Direction.ASC;

        if (sort != null && sort.length > 0) {
            String[] sortParts = sort[0].split(",");
            sortField = sortParts[0];
            if (sortParts.length > 1) {
                direction = sortParts[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            }
        }

        return PageRequest.of(page, size, direction, sortField);
    }

    // Phương thức tiện ích để tạo Sort từ tham số sort
    private Sort createSortFromParams(String[] sort) {
        Sort sortable = Sort.unsorted();
        if (sort != null && sort.length > 0) {
            String sortField = "id";
            Sort.Direction direction = Sort.Direction.ASC;

            if (sort[0].contains(",")) {
                String[] parts = sort[0].split(",");
                sortField = parts[0];
                direction = parts.length > 1 && parts[1].equalsIgnoreCase("desc") ?
                        Sort.Direction.DESC : Sort.Direction.ASC;
            } else {
                sortField = sort[0];
            }

            sortable = Sort.by(direction, sortField);
        }
        return sortable;
    }
}
