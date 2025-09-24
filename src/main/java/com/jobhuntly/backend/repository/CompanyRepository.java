package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.dto.response.LocationCompanyResponse;
import com.jobhuntly.backend.entity.Company;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByUser_Id(Long user_id);

    Optional<Company> findByUserId(Long userId);

    List<Company> findAllByCompanyNameIgnoreCase(String companyName);

    // Tìm công ty theo danh sách categoryId (bao gồm cả category cha và con)
    @Query("SELECT DISTINCT c FROM Company c JOIN c.categories cat " +
            "WHERE cat.id IN :categoryIds OR cat.parent.id IN :categoryIds")
    List<Company> findByCategoryIdsIncludingParents(@Param("categoryIds") List<Long> categoryIds);

    // Lấy danh sách các location của công ty (không trùng lặp)
    @Query("SELECT DISTINCT c.locationCity as locationCity FROM Company c WHERE c.locationCity IS NOT NULL AND c.locationCity <> ''")
    List<LocationCompanyResponse> findAllDistinctLocations();

    @Query("SELECT c FROM Company c WHERE LOWER(c.locationCity) = LOWER(:location)")
    List<Company> findByLocation(@Param("location") String location);

    @Query("SELECT DISTINCT c FROM Company c " +
            "LEFT JOIN c.categories cat " +
            "WHERE (:name IS NULL OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:categoryIdsEmpty = true OR cat.id IN :categoryIds OR cat.parent.id IN :categoryIds)")
    List<Company> searchCompanies(
            @Param("name") String name,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("categoryIdsEmpty") boolean categoryIdsEmpty);

    // Có phân trang
    Page<Company> findAllByCompanyNameIgnoreCase(String companyName, Pageable pageable);

    @Query("SELECT DISTINCT c FROM Company c JOIN c.categories cat " +
            "WHERE cat.id IN :categoryIds OR cat.parent.id IN :categoryIds")
    Page<Company> findByCategoryIdsIncludingParents(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);

    @Query("SELECT c FROM Company c WHERE LOWER(c.locationCity) = LOWER(:location)")
    Page<Company> findByLocation(@Param("location") String location, Pageable pageable);

    @Query("SELECT DISTINCT c FROM Company c " +
            "LEFT JOIN c.categories cat " +
            "WHERE (:name IS NULL OR LOWER(c.companyName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:categoryIdsEmpty = true OR cat.id IN :categoryIds OR cat.parent.id IN :categoryIds)")
    Page<Company> searchCompanies(
            @Param("name") String name,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("categoryIdsEmpty") boolean categoryIdsEmpty,
            Pageable pageable);

    // cập nhật thời gian vip cho company
    @Modifying
    @Transactional
    @Query("update Company c set c.vipUntil = :until where c.id = :companyId")
    int updateVipUntil(@Param("companyId") Long companyId, @Param("until") OffsetDateTime until);

    List<Company> findByIdIn(Collection<Long> ids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
                UPDATE Company c
                SET c.vipUntil = :newVipUntil,
                    c.proCompany = TRUE
                WHERE c.id = :companyId
                """)
    int upsertVipAndFlag(@Param("companyId") Long companyId,
                         @Param("newVipUntil") OffsetDateTime newVipUntil);

    // lấy owner (userId của recruiter sở hữu company) để check quyền
    @Query("select c.user.id from Company c where c.id = :companyId")
    Optional<Long> findOwnerUserIdById(@Param("companyId") Long companyId);


    @Modifying
    @Transactional
    @Query("""
        UPDATE Company c
        SET c.proCompany = FALSE
        WHERE c.vipUntil < :now
        """)
    int expireVip(@Param("now") OffsetDateTime now);
}
