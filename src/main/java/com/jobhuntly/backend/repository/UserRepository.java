package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    @Query("SELECT u FROM User u WHERE u.role.roleName = :roleName")
    Page<User> findAllByRole(@Param("roleName") String roleName, Pageable pageable);
}
