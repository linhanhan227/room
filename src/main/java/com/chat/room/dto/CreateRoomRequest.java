package com.chat.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomRequest {
    @NotBlank(message = "Room name is required")
    @Size(min = 1, max = 100, message = "Room name must be between 1 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    private String avatar;

    private String type;

    private String password;

    private Integer maxMembers;
}
