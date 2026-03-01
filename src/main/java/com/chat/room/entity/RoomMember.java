package com.chat.room.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(RoomMemberId.class)
@SQLDelete(sql = "UPDATE room_members SET deleted = true WHERE room_id = ? AND user_id = ?")
@Where(clause = "deleted = false")
public class RoomMember {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoom room;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20)
    @Builder.Default
    private MemberRole role = MemberRole.MEMBER;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    @Column(name = "muted")
    @Builder.Default
    private Boolean muted = false;

    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    public enum MemberRole {
        OWNER, ADMIN, MEMBER
    }

    @PrePersist
    public void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }
}
