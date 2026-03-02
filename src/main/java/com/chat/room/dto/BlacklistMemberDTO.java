package com.chat.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistMemberDTO {
    private Long id;
    private Long roomId;
    private String roomName;
    private Long userId;
    private String username;
    private String nickname;
    private Long addedById;
    private String addedByName;
    private String reason;
    private LocalDateTime createdAt;
}
