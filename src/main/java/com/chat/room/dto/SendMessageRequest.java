package com.chat.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendMessageRequest {
    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotBlank(message = "Message content is required")
    private String content;

    private String type;
}
