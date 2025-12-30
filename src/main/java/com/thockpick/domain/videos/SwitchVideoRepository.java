package com.thockpick.domain.videos;

import com.thockpick.domain.switches.Switch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 스위치-영상 연관 Repository
 */
@Repository
public interface SwitchVideoRepository extends JpaRepository<SwitchVideo, Long> {

    /**
     * 스위치로 연관된 영상 목록 조회
     */
    List<SwitchVideo> findBySwitchEntity(Switch switchEntity);

    /**
     * 영상으로 연관된 스위치 목록 조회
     */
    List<SwitchVideo> findByVideo(Video video);

    /**
     * 스위치와 영상으로 연관 관계 조회
     */
    Optional<SwitchVideo> findBySwitchEntityAndVideo(Switch switchEntity, Video video);
}
