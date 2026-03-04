package com.chat.room.dto;

import com.chat.room.entity.Report;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDTO {

    private Long id;
    private Long reporterId;
    private String reporterName;
    private Long reportedUserId;
    private String reportedUserName;
    private Long reportedRoomId;
    private String reportedRoomName;
    private Long reportedMessageId;
    private String type;
    private String targetType;
    private String reason;
    private String description;
    private String status;
    private Long handlerId;
    private String handlerName;
    private String handleResult;
    private LocalDateTime handledAt;
    private LocalDateTime createdAt;

    public static ReportDTO fromEntity(Report report) {
        ReportDTO dto = new ReportDTO();
        dto.setId(report.getId());
        dto.setReporterId(report.getReporterId());
        dto.setReportedUserId(report.getReportedUserId());
        dto.setReportedRoomId(report.getReportedRoomId());
        dto.setReportedMessageId(report.getReportedMessageId());
        dto.setType(report.getType() != null ? report.getType().name() : null);
        dto.setTargetType(report.getTargetType() != null ? report.getTargetType().name() : null);
        dto.setReason(report.getReason());
        dto.setDescription(report.getDescription());
        dto.setStatus(report.getStatus() != null ? report.getStatus().name() : null);
        dto.setHandlerId(report.getHandlerId());
        dto.setHandleResult(report.getHandleResult());
        dto.setHandledAt(report.getHandledAt());
        dto.setCreatedAt(report.getCreatedAt());
        return dto;
    }
}
