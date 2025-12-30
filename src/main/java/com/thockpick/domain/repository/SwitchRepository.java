package com.thockpick.domain.repository;

import com.thockpick.domain.entity.Switch;
import com.thockpick.domain.enums.SwitchType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 스위치 Repository
 */
@Repository
public interface SwitchRepository extends JpaRepository<Switch, Long> {

    /**
     * 이름으로 스위치 조회
     */
    Optional<Switch> findByName(String name);

    /**
     * 타입으로 스위치 목록 조회
     */
    List<Switch> findByType(SwitchType type);

    /**
     * 제조사로 스위치 목록 조회
     */
    List<Switch> findByManufacturer(String manufacturer);

    /**
     * 이름에 키워드가 포함된 스위치 목록 조회
     */
    List<Switch> findByNameContaining(String keyword);

    /**
     * Google Sheets 행 번호로 스위치 조회
     */
    Optional<Switch> findByGoogleSheetsRow(Integer googleSheetsRow);
}
