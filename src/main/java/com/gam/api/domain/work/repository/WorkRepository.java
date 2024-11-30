package com.gam.api.domain.work.repository;

import com.gam.api.domain.work.entity.Work;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WorkRepository extends JpaRepository<Work, Long> {
    Optional<Work> getWorkById(Long workId);
    void deleteById(Long workId);
    Optional<Work> getWorkByUserIdAndIsFirst(Long userId, Boolean status);
    List<Work> findByUserIdAndIsFirstOrderByCreatedAtDesc(Long userId, boolean isFirst);
    List<Work> findByUserIdAndIsFirstAndIsActiveOrderByCreatedAtDesc(Long userId, boolean isFirst, boolean isActive);
    @Query("SELECT w FROM Work w JOIN FETCH w.user where w.user.id = :userId")
    List<Work> findAllByUserId(Long userId);
    @Query(value = "SELECT w FROM Work w WHERE LOWER(w.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY w.createdAt DESC")
    List<Work> findByKeyword(String keyword);
    Optional<Work> findFirstByUserIdAndIsActiveOrderByCreatedAtDesc(Long userId, boolean isActive);

    @Modifying
    @Query("UPDATE Work w SET w.viewCount = w.viewCount + 1 WHERE w.id IN :ids")
    void updateWorksViewCount(@Param("ids") List<Long> ids);
}
