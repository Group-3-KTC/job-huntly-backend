package com.jobhuntly.backend.repository;

import com.jobhuntly.backend.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndReadAtIsNull(Long userId);

    // JPQL: sắp xếp unread trước rồi tới read, trong mỗi nhóm sort createdAt desc
    @Query("""
           SELECT n FROM Notification n
           WHERE n.userId = :userId
           ORDER BY CASE WHEN n.readAt IS NULL THEN 0 ELSE 1 END ASC,
                    n.createdAt DESC
           """)
    Page<Notification> findFeedByUser(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Query("""
           UPDATE Notification n
           SET n.readAt = CURRENT_TIMESTAMP
           WHERE n.userId = :userId AND n.notificationId IN :ids AND n.readAt IS NULL
           """)
    int markReadByIds(@Param("userId") Long userId, @Param("ids") Iterable<Long> ids);

    @Modifying
    @Query("""
           UPDATE Notification n
           SET n.readAt = CURRENT_TIMESTAMP
           WHERE n.userId = :userId AND n.readAt IS NULL
           """)
    int markAllRead(@Param("userId") Long userId);
}
