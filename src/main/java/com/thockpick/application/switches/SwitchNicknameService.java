package com.thockpick.application.switches;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SwitchNicknameService {

    private final ObjectMapper objectMapper;
    private List<NicknameRule> rules = new ArrayList<>();

    @Getter
    static class NicknameRule {
        private String keyword;
        private List<String> nicknames;
    }

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("data/switch_nicknames.json");
            rules = objectMapper.readValue(resource.getInputStream(), new TypeReference<List<NicknameRule>>() {});
        } catch (IOException e) {
            log.warn("별명 데이터 로드 실패: {}", e.getMessage());
        }
    }

    // 스위치 이름에 키워드가 포함되어 있으면 별명 리스트 반환
    public List<String> getNicknames(String switchName) {
        if (switchName == null) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        String lowerName = switchName.toLowerCase();

        for (NicknameRule rule : rules) {
            if (lowerName.contains(rule.getKeyword().toLowerCase())) {
                result.addAll(rule.getNicknames());
            }
        }
        return result;
    }
}