package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByUser_Id(Long user_id); // Kiểm tra company thuộc về người dùng nào đó chưa
}
