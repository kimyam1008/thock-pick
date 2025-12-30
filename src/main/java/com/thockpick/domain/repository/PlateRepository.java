package com.thockpick.domain.repository;

import com.thockpick.domain.entity.Plate;
import com.thockpick.domain.enums.PlateMaterial;
import com.thockpick.domain.enums.PlateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 보강판 Repository
 */
@Repository
public interface PlateRepository extends JpaRepository<Plate, Long> {

    /**
     * 재질로 보강판 목록 조회
     */
    List<Plate> findByMaterial(PlateMaterial material);

    /**
     * 타입으로 보강판 목록 조회
     */
    List<Plate> findByType(PlateType type);

    /**
     * 이름으로 보강판 조회
     */
    Optional<Plate> findByName(String name);

    /**
     * Google Sheets 행 번호로 보강판 조회
     */
    Optional<Plate> findByGoogleSheetsRow(Integer googleSheetsRow);
}
