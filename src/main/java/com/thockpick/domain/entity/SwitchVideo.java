package com.thockpick.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 스위치-영상 연관 테이블 (N:M)
 */
@Entity
@Table(name = "switch_videos", indexes = {
        @Index(name = "idx_switch_video_switch", columnList = "switch_id"),
        @Index(name = "idx_switch_video_video", columnList = "video_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "idx_switch_video_unique", columnNames = {"switch_id", "video_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class SwitchVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "switch_id", nullable = false)
    private Switch switchEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer relevanceScore;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
