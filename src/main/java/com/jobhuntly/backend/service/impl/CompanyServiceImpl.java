package com.jobhuntly.backend.service.impl;

import com.jobhuntly.backend.dto.response.CompanyDto;
import com.jobhuntly.backend.entity.Company;
import com.jobhuntly.backend.entity.User;
import com.jobhuntly.backend.exception.ResourceNotFoundException;
import com.jobhuntly.backend.mapper.CompanyMapper;
import com.jobhuntly.backend.repository.CompanyRepository;
import com.jobhuntly.backend.repository.UserRepository;
import com.jobhuntly.backend.service.company.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {
    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyMapper companyMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CompanyDto> getAllCompanies() {
        return companyMapper.toDtoList(companyRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyDto getCompanyById(Long id) {
        return companyMapper.toDto(companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company ID Not found: " + id)));
    }

    @Override
    @Transactional
    public CompanyDto createCompany(CompanyDto companyDto) {
        // ID là null khi tạo mới vô DB nó tự động tăng
        companyDto.setId(null);

        Long userId = companyDto.getUserId();
        if (userId == null) {
            throw new IllegalArgumentException("userId là bắt buộc");
        }

        // 1. Kiểm tra recruiter đã có công ty hay chưa TRƯỚC khi load User
        if (companyRepository.existsByUser_Id(userId)) {
            throw new IllegalArgumentException("Recruiter này đã có công ty");
        }

        // 2. Load User và kiểm tra role
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Chỉ cho phép RECRUITER tạo công ty
        if (!"RECRUITER".equalsIgnoreCase(user.getRole().getRoleName())) {
            throw new IllegalArgumentException("Chỉ RECRUITER mới được tạo công ty");
        }

        Company company = companyMapper.toEntity(companyDto);
        company.setUser(user);

        return companyMapper.toDto(companyRepository.save(company));
    }

    @Override
    @Transactional
    public CompanyDto updateCompanyById(Long id, CompanyDto companyDto) {
        return companyRepository.findById(id)
                .map(existing -> {
                    companyDto.setId(id);
                    companyMapper.updateEntityFromDto(companyDto, existing);

                    // Xử lý mối quan hệ với User nếu có thay đổi
                    if (companyDto.getUserId() != null) {
                        User user = userRepository.findById(companyDto.getUserId())
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + companyDto.getUserId()));
                        existing.setUser(user);
                    }
                    return companyMapper.toDto(companyRepository.save(existing));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Company ID Not found: " + id));
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
}
