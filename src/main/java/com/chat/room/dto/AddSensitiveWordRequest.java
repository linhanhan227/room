package com.chat.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddSensitiveWordRequest {
    @NotBlank(message = "Word is required")
    @Size(max = 100, message = "Word must be less than 100 characters")
    private String word;

    @Size(max = 50, message = "Category must be less than 50 characters")
    private String category;

    private Integer level;

    @Size(max = 100, message = "Replacement must be less than 100 characters")
    private String replacement;
}
