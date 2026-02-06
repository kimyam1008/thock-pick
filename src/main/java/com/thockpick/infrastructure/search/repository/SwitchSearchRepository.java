package com.thockpick.infrastructure.search.repository;

import com.thockpick.infrastructure.search.document.SwitchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface SwitchSearchRepository extends ElasticsearchRepository<SwitchDocument, Long> {
    /**
     * nicknames 필드에서 정확히 일치하는 키워드 검색 (Keyword 타입)
     */
    List<SwitchDocument> findByNicknames(String nickname);

    /**
     * name 필드에서 형태소 분석 후 매칭 (Text 타입)
     */
    List<SwitchDocument> findByName(String name);

    /**
     * category 필드에서 형태소 분석 후 매칭 (Text 타입)
     */
    List<SwitchDocument> findByCategory(String category);
}