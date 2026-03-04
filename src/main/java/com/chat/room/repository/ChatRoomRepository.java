package com.chat.room.repository;

import com.chat.room.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findByOwnerId(Long ownerId);

    @Query("SELECT r FROM ChatRoom r WHERE r.type = 'PUBLIC' AND r.status = 'ACTIVE'")
    Page<ChatRoom> findPublicRooms(Pageable pageable);

    @Query("SELECT r FROM ChatRoom r WHERE r.id IN (SELECT rm.room.id FROM RoomMember rm WHERE rm.user.id = :userId AND rm.deleted = false) AND r.status = 'ACTIVE'")
    List<ChatRoom> findByMemberId(@Param("userId") Long userId);

    @Query("SELECT r FROM ChatRoom r WHERE r.name LIKE %:keyword% AND r.type = 'PUBLIC'")
    Page<ChatRoom> searchPublicRooms(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(rm) FROM RoomMember rm WHERE rm.room.id = :roomId AND rm.deleted = false")
    Integer countMembersByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT CASE WHEN COUNT(rm) > 0 THEN true ELSE false END FROM RoomMember rm WHERE rm.room.id = :roomId AND rm.user.id = :userId AND rm.deleted = false")
    boolean isUserInRoom(@Param("roomId") Long roomId, @Param("userId") Long userId);

    Optional<ChatRoom> findByIdAndStatus(Long id, ChatRoom.RoomStatus status);

    boolean existsByName(String name);

    @Query("SELECT r, COUNT(rm) as memberCount FROM ChatRoom r LEFT JOIN RoomMember rm ON rm.room = r AND rm.deleted = false " +
           "WHERE r.type = 'PUBLIC' AND r.status = 'ACTIVE' " +
           "GROUP BY r.id ORDER BY memberCount DESC")
    List<Object[]> findRoomsWithMemberCount(Pageable pageable);

    @Query("SELECT r, COUNT(msg) as messageCount FROM ChatRoom r LEFT JOIN Message msg ON msg.room = r " +
           "WHERE r.type = 'PUBLIC' AND r.status = 'ACTIVE' " +
           "AND msg.createdAt >= :since " +
           "GROUP BY r.id ORDER BY messageCount DESC")
    List<Object[]> findRoomsWithRecentActivity(@Param("since") LocalDateTime since, Pageable pageable);

    @Query("SELECT r FROM ChatRoom r WHERE r.type = 'PUBLIC' AND r.status = 'ACTIVE' ORDER BY r.createdAt DESC")
    List<ChatRoom> findNewestPublicRooms(Pageable pageable);

    @Query("SELECT r FROM ChatRoom r WHERE r.type = 'PUBLIC' AND r.status = 'ACTIVE' " +
           "AND r.id NOT IN (SELECT rm.room.id FROM RoomMember rm WHERE rm.user.id = :userId)")
    List<ChatRoom> findPublicRoomsNotJoinedByUser(@Param("userId") Long userId, Pageable pageable);
}
