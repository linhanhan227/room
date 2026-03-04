package com.chat.room.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;


@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE chat_rooms SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "avatar", length = 255)
    private String avatar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    @Builder.Default
    private RoomType type = RoomType.PUBLIC;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "max_members")
    @Builder.Default
    private Integer maxMembers = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private RoomStatus status = RoomStatus.ACTIVE;

    @Column(name = "deleted")
    @Builder.Default
    private Boolean deleted = false;

    public enum RoomType {
        PUBLIC, PRIVATE, GROUP
    }

    public enum RoomStatus {
        ACTIVE, INACTIVE, ARCHIVED
    }
}
