package com.chat.room.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "system_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private User operator;

    @Column(name = "target_user_id")
    private Long targetUserId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 20)
    @Builder.Default
    private LogLevel level = LogLevel.INFO;

    public enum LogLevel {
        INFO, WARNING, ERROR, CRITICAL
    }
}
