package com.chat.room.dto;

import com.chat.room.entity.SystemLog;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemLogDTO {
    private Long id;
    private String action;
    private String entityType;
    private Long entityId;
    private Long operatorId;
    private String operatorName;
    private Long targetUserId;
    private String details;
    private String ipAddress;
    private SystemLog.LogLevel level;
    private LocalDateTime createdAt;

    public static SystemLogDTO fromEntity(SystemLog log) {
        return SystemLogDTO.builder()
                .id(log.getId())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .operatorId(log.getOperator() != null ? log.getOperator().getId() : null)
                .operatorName(log.getOperator() != null ? 
                        (log.getOperator().getNickname() != null ? log.getOperator().getNickname() : log.getOperator().getUsername()) : null)
                .targetUserId(log.getTargetUserId())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .level(log.getLevel())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
