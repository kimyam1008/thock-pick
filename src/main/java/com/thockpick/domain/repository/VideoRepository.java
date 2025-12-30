package com.thockpick.domain.repository;

import com.thockpick.domain.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 유튜브 영상 Repository
 */
@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    /**
     * 유튜브 ID로 영상 조회
     */
    Optional<Video> findByYoutubeId(String youtubeId);

    /**
     * URL로 영상 조회
     */
    Optional<Video> findByUrl(String url);

    /**
     * Google Sheets 행 번호로 영상 조회
     */
    Optional<Video> findByGoogleSheetsRow(Integer googleSheetsRow);
}
