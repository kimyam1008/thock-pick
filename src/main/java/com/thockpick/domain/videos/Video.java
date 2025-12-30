package com.thockpick.domain.videos;

import com.thockpick.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 유튜브 영상 엔티티
 */
@Entity
@Table(name = "videos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Video extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 500)
    private String url;

    @Column(unique = true, length = 20)
    private String youtubeId;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(length = 100)
    private String channelName;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer viewCount;

    private LocalDateTime publishedAt;

    private Integer duration;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer googleSheetsRow;

    @OneToMany(mappedBy = "video", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SwitchVideo> switchVideos = new ArrayList<>();
}
