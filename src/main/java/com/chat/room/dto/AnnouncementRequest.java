package com.chat.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String type;

    private String priority;

    private Boolean isPinned;

    private Boolean isPublished;

    private String publishAt;

    private String expireAt;
}
