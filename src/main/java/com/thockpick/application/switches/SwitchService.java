package com.thockpick.application.switches;

import com.thockpick.domain.switches.Switch;
import com.thockpick.domain.switches.SwitchRepository;
import com.thockpick.domain.switches.SwitchType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

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
     * 스위치 목록 통합 검색 (동적 쿼리 적용)
     * 조건(타입, 키워드)이 있으면 WHERE 절에 추가하고, 없으면 무시합니다.
     *
     * @param type     스위치 타입 (null이면 전체)
     * @param keyword  검색 키워드 (null/empty면 전체)
     * @param pageable 페이징 정보
     * @return 필터링된 스위치 목록 Page 객체
     */
    public Page<Switch> searchSwitches(SwitchType type, String manufacturer, String keyword, Pageable pageable) {
        Specification<Switch> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 타입 조건
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }

            // 2. 제조사 조건 (추가됨)
            if (StringUtils.hasText(manufacturer)) {
                predicates.add(criteriaBuilder.equal(root.get("manufacturer"), manufacturer));
            }

            // 3. 키워드 검색 조건
            if (StringUtils.hasText(keyword)) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + keyword + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return switchRepository.findAll(spec, pageable);
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
