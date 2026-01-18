package com.thockpick.application.switches;

import com.thockpick.domain.switches.Switch;
import com.thockpick.domain.switches.SwitchRepository;
import com.thockpick.domain.switches.SwitchType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 스위치 애플리케이션 서비스
 * (유스케이스: 조회, 검색, 필터링 등)
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SwitchService {

    private final SwitchRepository switchRepository;

    /**
     * 전체 스위치 목록 조회
     */
    public List<Switch> findAllSwitches() {
        return switchRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    /**
     * 타입별 스위치 목록 조회
     */
    public List<Switch> findSwitchesByType(SwitchType type) {
        return switchRepository.findByType(type);
    }

    /**
     * 제조사별 스위치 목록 조회
     */
    public List<Switch> findSwitchesByManufacturer(String manufacturer) {
        return switchRepository.findByManufacturer(manufacturer);
    }

    /**
     * 키워드로 스위치 검색
     */
    public List<Switch> searchSwitches(String keyword) {
        return switchRepository.findByNameContaining(keyword);
    }

    /**
     * 스위치 상세 조회
     */
    public Switch findSwitchById(Long id) {
        return switchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("스위치를 찾을 수 없습니다: " + id));
    }

    /**
     * 전체 스위치 개수 조회
     */
    public long countAllSwitches() {
        return switchRepository.count();
    }

    /**
     * 타입별 스위치 개수 조회 (최적화: COUNT 쿼리 사용)
     */
    public long countByType(SwitchType type) {
        return switchRepository.countByType(type);
    }
}
