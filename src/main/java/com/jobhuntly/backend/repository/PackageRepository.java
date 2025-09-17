package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.PackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PackageRepository extends JpaRepository<PackageEntity, Long> {
    Optional<PackageEntity> findByCodeAndIsActiveTrue(String code);

    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCaseAndPackageIdNot(String code, Long packageId);
}
