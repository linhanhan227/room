package com.chat.room.dto;

import com.chat.room.entity.Report;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequest {

    @NotNull(message = "举报类型不能为空")
    private String type;

    @NotNull(message = "举报目标类型不能为空")
    private String targetType;

    private Long reportedUserId;

    private Long reportedRoomId;

    private Long reportedMessageId;

    @NotBlank(message = "举报原因不能为空")
    private String reason;

    private String description;
}
