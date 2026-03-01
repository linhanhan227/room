package com.chat.room.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sensitive_words")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensitiveWord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word", nullable = false, unique = true, length = 100)
    private String word;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "level")
    @Builder.Default
    private Integer level = 1;

    @Column(name = "replacement", length = 100)
    private String replacement;

    @Column(name = "enabled")
    @Builder.Default
    private Boolean enabled = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}
