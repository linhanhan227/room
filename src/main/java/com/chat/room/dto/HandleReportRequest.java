package com.chat.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HandleReportRequest {

    @NotNull(message = "处理状态不能为空")
    private String status;

    @NotBlank(message = "处理结果不能为空")
    private String handleResult;
}
