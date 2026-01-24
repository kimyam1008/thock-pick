package com.thockpick.application.switches;

import com.thockpick.domain.switches.Switch;
import com.thockpick.domain.switches.SwitchRepository;
import com.thockpick.domain.switches.SwitchType;
import com.thockpick.infrastructure.search.document.SwitchDocument;
import com.thockpick.infrastructure.search.repository.SwitchSearchRepository;
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

    private final SwitchRepository switchRepository;            // MariaDB
    private final SwitchSearchRepository searchRepository;      // Elasticsearch

    /**
     * 스위치 목록 통합 검색 (ES + MariaDB 하이브리드)
     *
     * @param type     스위치 타입 (null이면 전체)
     * @param keyword  검색 키워드 (null/empty면 전체)
     * @param pageable 페이징 정보
     * @return 필터링된 스위치 목록 Page 객체
     */
    public Page<Switch> searchSwitches(SwitchType type, String manufacturer, String keyword, Pageable pageable) {

        // 검색어(keyword)가 있는 경우: Elasticsearch 사용
        if (StringUtils.hasText(keyword)) {

            // ES에서 검색 수행 (이름 OR 별명 OR 카테고리)
            List<SwitchDocument> esResults = searchRepository.findByNameOrNicknamesOrCategory(keyword, keyword, keyword);

            if (esResults.isEmpty()) {
                return Page.empty(pageable); // 검색 결과가 없으면 빈 페이지 반환
            }

            // 검색된 문서들의 ID 추출
            List<Long> switchIds = esResults.stream()
                    .map(SwitchDocument::getId)
                    .toList();

            // 추출한 ID로 MariaDB 조회 (Specification 구성)
            Specification<Switch> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                // ES에서 찾은 ID들에 해당하는 데이터만 가져오기
                predicates.add(root.get("id").in(switchIds));

                // 나머지 필터 조건 (타입, 제조사) 추가 적용
                if (type != null) {
                    predicates.add(criteriaBuilder.equal(root.get("type"), type));
                }
                if (StringUtils.hasText(manufacturer)) {
                    predicates.add(criteriaBuilder.equal(root.get("manufacturer"), manufacturer));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            return switchRepository.findAll(spec, pageable);
        }

        // 검색어가 없는 경우: 기존 MariaDB 필터링만 수행
        else {
            Specification<Switch> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (type != null) {
                    predicates.add(criteriaBuilder.equal(root.get("type"), type));
                }
                if (StringUtils.hasText(manufacturer)) {
                    predicates.add(criteriaBuilder.equal(root.get("manufacturer"), manufacturer));
                }

                // (keyword 조건은 여기서 빠짐)

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };

            return switchRepository.findAll(spec, pageable);
        }
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
