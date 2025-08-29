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

        // Trả về categoryIds cho tiện cập nhật phía client:
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
        // ID null để DB tự tăng
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

        // Chỉ cho phép RECRUITER tạo công ty
        if (!"RECRUITER".equalsIgnoreCase(user.getRole().getRoleName())) {
            throw new IllegalArgumentException("Chỉ tạo công ty mới cho RECRUITER");
        }

        // 3) Map DTO -> Entity (không set user/categories trong mapper)
        Company entity = companyMapper.toEntity(companyDto);
        entity.setUser(user);

        // 4) Xử lý N–N: set categories từ categoryIds (nếu có)
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

        // 1) Patch field non-null (mapper IGNORE nulls)
        companyDto.setId(id);
        companyMapper.updateEntityFromDto(companyDto, existing);

        // 2) Đổi user nếu gửi userId (optional)
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

        // 3) Cập nhật N–N nếu client gửi categoryIds
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
            return getAllCompanies(); // Trả về tất cả nếu không có filter
        }
        // Tìm công ty theo cả category cha và con
        List<Company> companies = companyRepository.findByCategoryIdsIncludingParents(categoryIds);
        List<CompanyDto> dtos = companyMapper.toDtoList(companies);

        // Bổ sung thông tin thêm nếu cần
        for (int i = 0; i < companies.size(); i++) {
            Company company = companies.get(i);
            CompanyDto dto = dtos.get(i);

            // Thêm số lượng job
            dto.setJobsCount(jobRepository.countJobsByCompanyId(company.getId()));

            // Thêm categoryIds
            if (company.getCategories() != null) {
                dto.setCategoryIds(company.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet()));
            }
        }

        return dtos;
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
        List<CompanyDto> dtos = companyMapper.toDtoList(companies);

        // Bổ sung thông tin thêm
        for (int i = 0; i < companies.size(); i++) {
            Company company = companies.get(i);
            CompanyDto dto = dtos.get(i);

            // Thêm số lượng job
            dto.setJobsCount(jobRepository.countJobsByCompanyId(company.getId()));

            // Thêm categoryIds
            if (company.getCategories() != null) {
                dto.setCategoryIds(company.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet()));
            }
        }

        return dtos;
    }

    @Override
    public List<CompanyDto> getCompaniesByName(String name) {
        List<Company> companies = companyRepository.findAllByCompanyNameIgnoreCase(name);
        List<CompanyDto> dtos = companyMapper.toDtoList(companies);

        // Bổ sung thông tin thêm
        for (int i = 0; i < companies.size(); i++) {
            Company company = companies.get(i);
            CompanyDto dto = dtos.get(i);

            // Thêm số lượng job
            dto.setJobsCount(jobRepository.countJobsByCompanyId(company.getId()));

            // Thêm categoryIds
            if (company.getCategories() != null) {
                dto.setCategoryIds(company.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet()));
            }
        }
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDto> getCompaniesByNameOrCategory(String name, List<Long> categoryIds) {
        // Kiểm tra danh sách rỗng
        boolean isCategoryIdsEmpty = categoryIds == null || categoryIds.isEmpty();

        // Đảm bảo categoryIds không null cho tham số truy vấn
        List<Long> safeList = categoryIds == null ? java.util.Collections.emptyList() : categoryIds;

        // Tìm kiếm theo tên và/hoặc ngành nghề
        List<Company> companies = companyRepository.searchCompanies(
                name == null || name.trim().isEmpty() ? null : name.trim(),
                safeList,
                isCategoryIdsEmpty);

        List<CompanyDto> dtos = companyMapper.toDtoList(companies);

        // Bổ sung thông tin thêm
        for (int i = 0; i < companies.size(); i++) {
            Company company = companies.get(i);
            CompanyDto dto = dtos.get(i);

            // Thêm số lượng job
            dto.setJobsCount(jobRepository.countJobsByCompanyId(company.getId()));

            // Thêm categoryIds
            if (company.getCategories() != null) {
                dto.setCategoryIds(company.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet()));
            }
        }

        return dtos;
    }
}
