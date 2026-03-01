package com.chat.room.repository;

import com.chat.room.entity.RoomMember;
import com.chat.room.entity.RoomMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomMemberRepository extends JpaRepository<RoomMember, RoomMemberId> {

    List<RoomMember> findByRoomId(Long roomId);

    List<RoomMember> findByUserId(Long userId);

    Optional<RoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    @Modifying
    @Query("DELETE FROM RoomMember rm WHERE rm.room.id = :roomId AND rm.user.id = :userId")
    int deleteByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM RoomMember rm WHERE rm.room.id = :roomId")
    int deleteByRoomId(@Param("roomId") Long roomId);

    @Modifying
    @Query("UPDATE RoomMember rm SET rm.lastReadAt = CURRENT_TIMESTAMP WHERE rm.room.id = :roomId AND rm.user.id = :userId")
    int updateLastReadAt(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Query("SELECT rm.role FROM RoomMember rm WHERE rm.room.id = :roomId AND rm.user.id = :userId")
    Optional<RoomMember.MemberRole> findRoleByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
