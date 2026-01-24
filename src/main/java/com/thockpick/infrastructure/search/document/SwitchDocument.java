package com.thockpick.infrastructure.search.document;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Getter
@Builder
@Document(indexName = "switches")
public class SwitchDocument {

    @Id
    private Long id; // MariaDB의 ID와 동일하게 사용

    @Field(type = FieldType.Text, analyzer = "nori") // 한글 형태소 분석
    private String name;

    @Field(type = FieldType.Keyword)
    private String brand;

    // 카테고리 필드 (저소음, HMX, 체리 등)
    @Field(type = FieldType.Text, analyzer = "nori")
    private String category;

    @Field(type = FieldType.Text, analyzer = "nori")
    private List<String> nicknames; // 우리가 주입한 별명들
}