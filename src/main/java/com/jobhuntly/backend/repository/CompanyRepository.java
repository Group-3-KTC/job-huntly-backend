package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.dto.response.LocationCompanyResponse;
import com.jobhuntly.backend.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByUser_Id(Long user_id); // Kiểm tra company thuộc về người dùng nào đó chưa
    List<Company> findAllByCompanyNameIgnoreCase(String companyName);
    // Tìm công ty theo danh sách categoryId (bao gồm cả category cha và con)
    @Query("SELECT DISTINCT c FROM Company c JOIN c.categories cat " +
            "WHERE cat.id IN :categoryIds OR cat.parent.id IN :categoryIds")
    List<Company> findByCategoryIdsIncludingParents(@Param("categoryIds") List<Long> categoryIds);

    // Lấy danh sách các location của công ty (không trùng lặp)
    @Query("SELECT DISTINCT c.locationCity as locationCity FROM Company c WHERE c.locationCity IS NOT NULL AND c.locationCity <> ''")
    List<LocationCompanyResponse> findAllDistinctLocations();

    // Tìm công ty theo location
    @Query("SELECT c FROM Company c WHERE LOWER(c.locationCity) = LOWER(:location)")
    List<Company> findByLocation(@Param("location") String location);

    // Tìm kiếm công ty theo tên hoặc ngành nghề
    @Query("SELECT DISTINCT c FROM Company c " +
            "LEFT JOIN c.categories cat " +
            "WHERE (:name IS NULL OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:categoryIdsEmpty = true OR cat.id IN :categoryIds OR cat.parent.id IN :categoryIds)")
    List<Company> searchCompanies(
            @Param("name") String name,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("categoryIdsEmpty") boolean categoryIdsEmpty);
}
