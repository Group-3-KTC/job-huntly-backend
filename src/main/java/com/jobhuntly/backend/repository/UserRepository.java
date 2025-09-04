package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByActivationToken(String token);

    Optional<User> findByGoogleId(String googleId);

    @Modifying
    @Query("""
      update User u
         set u.activationToken = null,
             u.activationTokenExpiresAt = null
       where u.id = :id
    """)
    int clearActivationToken(@Param("id") Long id);
}
