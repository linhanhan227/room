package com.chat.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MuteMemberRequest {
    private Long userId;
    private String reason;
    private Integer durationMinutes;
}
