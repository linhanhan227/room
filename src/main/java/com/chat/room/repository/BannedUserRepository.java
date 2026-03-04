package com.chat.room.repository;

import com.chat.room.entity.BannedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BannedUserRepository extends JpaRepository<BannedUser, Long> {

    List<BannedUser> findByUserId(Long userId);

    Optional<BannedUser> findByUserIdAndActiveTrue(Long userId);

    @Query("SELECT bu FROM BannedUser bu WHERE bu.user.id = :userId AND bu.active = true AND (bu.type = 'PERMANENT' OR bu.endTime > :now)")
    Optional<BannedUser> findActiveBanByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(bu) > 0 THEN true ELSE false END FROM BannedUser bu WHERE bu.user.id = :userId AND bu.active = true AND (bu.type = 'PERMANENT' OR bu.endTime > :now)")
    boolean isUserBanned(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    Page<BannedUser> findByActiveTrue(Pageable pageable);

    @Query("SELECT COUNT(bu) FROM BannedUser bu WHERE bu.active = true")
    Long countActiveBans();

    Page<BannedUser> findByBannedById(Long bannedById, Pageable pageable);

    @Query("SELECT bu FROM BannedUser bu WHERE bu.type = :type AND bu.active = true")
    List<BannedUser> findActiveBansByType(@Param("type") BannedUser.BanType type);

    @Modifying
    @Query("UPDATE BannedUser bu SET bu.active = false WHERE bu.user.id = :userId AND bu.active = true")
    int unbanUser(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE BannedUser bu SET bu.active = false WHERE bu.endTime < :now AND bu.type = 'TEMPORARY'")
    int expireTemporaryBans(@Param("now") LocalDateTime now);
}
