package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WardRepository extends JpaRepository<Ward, Long> {
    List<Ward> findByCity_NameIgnoreCase(String cityName);
}
