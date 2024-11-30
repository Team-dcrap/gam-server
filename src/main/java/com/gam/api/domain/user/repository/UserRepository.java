package com.gam.api.domain.user.repository;

import com.gam.api.domain.user.entity.User;
import com.gam.api.domain.user.entity.UserStatus;
import com.gam.api.domain.user.dto.query.UserScrapUserQueryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  
    Optional<User> getUserById(Long userId);
    boolean existsByUserName(String userName);

    @Query("SELECT u FROM User u JOIN FETCH u.works WHERE u.id = :userId")
    Optional<User> getUserByIdWithWorks(@Param("userId") Long userId);

    List<User> findByUserStatusAndFirstWorkIdIsNotNullOrderByScrapCountDesc(UserStatus userStatus);

    @Query(value = "SELECT u FROM User u WHERE LOWER(u.userName) LIKE %:keyword% ORDER BY u.createdAt DESC")
    List<User> findByUserName(@Param("keyword") String keyword);
  
    @Query(value = "SELECT u FROM User u WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY u.createdAt DESC")
    List<User> findByKeyWord(@Param("keyword") String keyword);
  
    @Query("SELECT u FROM User u JOIN FETCH u.works WHERE u.userStatus = :userStatus")
    List<User> findAllByUserStatusWithWorks( @Param("userStatus") UserStatus userStatus);

    @Query("select distinct new com.gam.api.domain.user.dto.query.UserScrapUserQueryDto(us, 0L, coalesce(userScrap.status, false) as scrapStatus, us.selectedFirstAt) " +
            "from User us " +
            "JOIN Work work " +
            "    on work.user.id = us.id " +
            "left join UserScrap userScrap" +
            "    on userScrap.targetId = us.id and userScrap.user.id = :userId " +
            "WHERE us.userStatus = 'PERMITTED' and us.role = 'USER' " +
            "and us.id not in ( " +
            "    select block.targetId " +
            "    from Block block " +
            "    where block.user.id = :userId and block.status = true) " +
            "GROUP BY us.id, userScrap.status " +
            "order by us.selectedFirstAt desc")
    List<UserScrapUserQueryDto> findAllDiscoveryUser(@Param("userId") long userId);


    @Query("select distinct new com.gam.api.domain.user.dto.query.UserScrapUserQueryDto(us, count(userTag.tag.id) as correctCount, coalesce(userScrap.status, false) as scrapStatus, us.selectedFirstAt) " +
            "from User us " +
            "JOIN Work work " +
            "    on work.user.id = us.id " +
            "JOIN UserTag userTag " +
            "    ON userTag.user.id = us.id " +
            "left join UserScrap userScrap" +
            "    on userScrap.targetId = us.id and userScrap.user.id = :userId " +
            "WHERE userTag.tag.id IN :tag " +
            "  and us.userStatus='PERMITTED' and us.role = 'USER' " +
            "and us.id not in ( " +
            "    select block.targetId " +
            "    from Block block " +
            "    where block.user.id = :userId and block.status = true) " +
            "GROUP BY us.id, userScrap.status " +
            "order by correctCount desc, us.selectedFirstAt desc")
    List<UserScrapUserQueryDto>findAllDiscoveryUserWithTag(@Param("userId") long userId, @Param("tag") int[] tag);

    List<User> findAll();

    @Query(value = "SELECT u FROM User u WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) and u.userStatus!='REPORTED' and u.id!=:userId ORDER BY u.createdAt DESC")
    List<User> findByKeyWord(@Param("userId")Long userId, @Param("keyword") String keyword);

    @Query("select u "+
            " from User u "+
            " where u.id in :userId and u.userStatus!='REPORTED' and u.userStatus!='WITHDRAWAL'")
    List<User> getByUserIdList(@Param("userId") List<Long> userId);

    void deleteById(Long userId);

}
