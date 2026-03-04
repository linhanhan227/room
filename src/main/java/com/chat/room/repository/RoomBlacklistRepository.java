package com.chat.room.repository;

import com.chat.room.entity.RoomBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomBlacklistRepository extends JpaRepository<RoomBlacklist, Long> {

    Optional<RoomBlacklist> findByRoomIdAndUserId(Long roomId, Long userId);

    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    List<RoomBlacklist> findByRoomId(Long roomId);

    List<RoomBlacklist> findByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM RoomBlacklist rb WHERE rb.room.id = :roomId AND rb.user.id = :userId")
    int deleteByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM RoomBlacklist rb WHERE rb.room.id = :roomId")
    int deleteByRoomId(@Param("roomId") Long roomId);
}
