package com.thockpick.domain.switches;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 스위치 Repository
 */
@Repository
public interface SwitchRepository extends JpaRepository<Switch, Long>, JpaSpecificationExecutor<Switch> {

    /**
     * 이름으로 스위치 조회
     */
    Optional<Switch> findByName(String name);

    /**
     * Google Sheets 행 번호와 카테고리로 스위치 조회 (탭별 행 구분)
     */
    Optional<Switch> findByGoogleSheetsRowAndCategory(Integer googleSheetsRow, String category);

    /**
     * Google Sheets 행 번호로 스위치 조회
     */
    Optional<Switch> findByGoogleSheetsRow(Integer googleSheetsRow);

    /**
     * 타입별 스위치 개수 조회 (COUNT 쿼리 사용)
     */
    long countByType(SwitchType type);

}
