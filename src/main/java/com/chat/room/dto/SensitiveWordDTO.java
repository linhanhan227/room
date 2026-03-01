package com.chat.room.dto;

import com.chat.room.entity.SensitiveWord;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensitiveWordDTO {
    private Long id;
    private String word;
    private String category;
    private Integer level;
    private String replacement;
    private Boolean enabled;

    public static SensitiveWordDTO fromEntity(SensitiveWord word) {
        return SensitiveWordDTO.builder()
                .id(word.getId())
                .word(word.getWord())
                .category(word.getCategory())
                .level(word.getLevel())
                .replacement(word.getReplacement())
                .enabled(word.getEnabled())
                .build();
    }
}
