package com.chat.room.dto;

import com.chat.room.entity.BannedUser;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanUserRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @Size(max = 500, message = "Reason must be less than 500 characters")
    private String reason;

    @NotNull(message = "Ban type is required")
    private BannedUser.BanType type;

    private LocalDateTime endTime;
}
