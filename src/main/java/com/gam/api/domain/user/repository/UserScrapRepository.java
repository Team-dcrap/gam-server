package com.gam.api.domain.user.repository;

import com.gam.api.domain.user.entity.User;
import com.gam.api.domain.user.entity.UserScrap;
import com.gam.api.domain.user.dto.query.UserScrapQueryDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserScrapRepository extends JpaRepository<UserScrap, Long> {
    List<UserScrap> findUserScrapsByUserId(Long userId);
    UserScrap findByUser_idAndTargetId(Long userId, Long targetId);

    UserScrap findByUserAndTargetId(User user, Long targetId);

    @Query("SELECT DISTINCT NEW com.gam.api.domain.user.dto.query.UserScrapQueryDto(us.id, us.modifiedAt, us.targetId) "+
            "FROM UserScrap us " +
            "LEFT JOIN FETCH User tu ON us.targetId = tu.id " +
            "LEFT JOIN Block b ON us.user.id = b.user.id " +
            "LEFT JOIN Report r ON us.targetId = r.targetUser.id " +
            "WHERE us.user.id = :userId " +
            "  AND us.status = true " +
            "  AND us.targetId NOT IN (SELECT bb.targetId FROM Block bb WHERE bb.user.id = :userId AND bb.status = true) " +
            "  AND us.targetId NOT IN (SELECT rr.targetUser.id FROM Report rr WHERE rr.status = 'END') ")
    List<UserScrapQueryDto> findUserScrapsExceptBlockUser(@Param("userId") long userId);

    void deleteAllByTargetId(Long targetId);
    void deleteAllByUserId(Long userId);

}
