package com.chat.room.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {
    private Long totalUsers;
    private Long onlineUsers;
    private Long totalRooms;
    private Long activeRooms;
    private Long totalMessages;
    private Long todayMessages;
    private Long bannedUsers;
    private List<ActiveRoomStats> topActiveRooms;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActiveRoomStats {
        private Long roomId;
        private String roomName;
        private Long messageCount;
        private Integer memberCount;
    }
}
