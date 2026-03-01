package com.chat.room.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "banned_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannedUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banned_by", nullable = false)
    private User bannedBy;

    @Column(name = "reason", length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    @Builder.Default
    private BanType type = BanType.TEMPORARY;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    public enum BanType {
        PERMANENT, TEMPORARY, WARNING
    }

    public boolean isExpired() {
        if (type == BanType.PERMANENT) {
            return false;
        }
        return endTime != null && endTime.isBefore(LocalDateTime.now());
    }
}
