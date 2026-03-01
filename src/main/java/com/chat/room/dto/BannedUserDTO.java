package com.chat.room.dto;

import com.chat.room.entity.BannedUser;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannedUserDTO {
    private Long id;
    private Long userId;
    private String username;
    private String userNickname;
    private Long bannedById;
    private String bannedByName;
    private String reason;
    private BannedUser.BanType type;
    private LocalDateTime endTime;
    private Boolean active;
    private LocalDateTime createdAt;

    public static BannedUserDTO fromEntity(BannedUser bannedUser) {
        return BannedUserDTO.builder()
                .id(bannedUser.getId())
                .userId(bannedUser.getUser().getId())
                .username(bannedUser.getUser().getUsername())
                .userNickname(bannedUser.getUser().getNickname())
                .bannedById(bannedUser.getBannedBy().getId())
                .bannedByName(bannedUser.getBannedBy().getNickname() != null ? 
                        bannedUser.getBannedBy().getNickname() : bannedUser.getBannedBy().getUsername())
                .reason(bannedUser.getReason())
                .type(bannedUser.getType())
                .endTime(bannedUser.getEndTime())
                .active(bannedUser.getActive())
                .createdAt(bannedUser.getCreatedAt())
                .build();
    }
}
