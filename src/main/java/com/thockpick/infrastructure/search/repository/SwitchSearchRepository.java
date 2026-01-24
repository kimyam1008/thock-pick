package com.thockpick.infrastructure.search.repository;

import com.thockpick.infrastructure.search.document.SwitchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface SwitchSearchRepository extends ElasticsearchRepository<SwitchDocument, Long> {
    // 이름 OR 별명 OR 카테고리 중 하나라도 매칭되면 검색
    List<SwitchDocument> findByNameOrNicknamesOrCategory(String name, String nickname, String category);
}