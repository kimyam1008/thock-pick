package com.thockpick.infrastructure.search.repository;

import com.thockpick.infrastructure.search.document.SwitchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface SwitchSearchRepository extends ElasticsearchRepository<SwitchDocument, Long> {
    // 이름이나 별명에 검색어가 포함된 것 찾기
    List<SwitchDocument> findByNameContainsOrNicknamesContains(String name, String nickname);
}