package com.chat.room.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "reported_user_id")
    private Long reportedUserId;

    @Column(name = "reported_room_id")
    private Long reportedRoomId;

    @Column(name = "reported_message_id")
    private Long reportedMessageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ReportType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "handler_id")
    private Long handlerId;

    @Column(name = "handle_result", length = 500)
    private String handleResult;

    @Column(name = "handled_at")
    private LocalDateTime handledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ReportType {
        SPAM,
        HARASSMENT,
        INAPPROPRIATE_CONTENT,
        VIOLENCE,
        FRAUD,
        OTHER
    }

    public enum TargetType {
        USER,
        ROOM,
        MESSAGE
    }

    public enum ReportStatus {
        PENDING,
        PROCESSING,
        RESOLVED,
        REJECTED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
