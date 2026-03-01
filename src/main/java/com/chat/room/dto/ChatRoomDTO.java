package com.chat.room.dto;

import com.chat.room.entity.ChatRoom;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDTO {
    private Long id;
    private String name;
    private String description;
    private String avatar;
    private Long ownerId;
    private String ownerName;
    private ChatRoom.RoomType type;
    private Integer maxMembers;
    private ChatRoom.RoomStatus status;
    private Integer memberCount;
    private LocalDateTime createdAt;

    public static ChatRoomDTO fromEntity(ChatRoom room) {
        return ChatRoomDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .description(room.getDescription())
                .avatar(room.getAvatar())
                .ownerId(room.getOwner().getId())
                .ownerName(room.getOwner().getNickname() != null ? room.getOwner().getNickname() : room.getOwner().getUsername())
                .type(room.getType())
                .maxMembers(room.getMaxMembers())
                .status(room.getStatus())
                .createdAt(room.getCreatedAt())
                .build();
    }
}
