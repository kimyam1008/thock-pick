package com.thockpick.application.switches;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SwitchNicknameService.getNicknames 의 키워드 부분매칭 동작을 캡처하는 특성화 테스트.
 * 룰은 JSON 로딩 대신 ReflectionTestUtils 로 직접 주입해 로직만 격리 검증한다.
 */
class SwitchNicknameServiceTest {

    private SwitchNicknameService service;

    @BeforeEach
    void setUp() {
        service = new SwitchNicknameService(new ObjectMapper());
        ReflectionTestUtils.setField(service, "rules", new ArrayList<>(List.of(
                rule("Brown", List.of("갈축", "체갈")),
                rule("Silent Red", List.of("저적", "체리저적"))
        )));
    }

    private SwitchNicknameService.NicknameRule rule(String keyword, List<String> nicknames) {
        SwitchNicknameService.NicknameRule r = new SwitchNicknameService.NicknameRule();
        ReflectionTestUtils.setField(r, "keyword", keyword);
        ReflectionTestUtils.setField(r, "nicknames", nicknames);
        return r;
    }

    @Test
    void 이름이_null이면_빈_리스트() {
        assertThat(service.getNicknames(null)).isEmpty();
    }

    @Test
    void 매칭되는_키워드가_없으면_빈_리스트() {
        assertThat(service.getNicknames("Gateron Yellow")).isEmpty();
    }

    @Test
    void 키워드가_이름에_포함되면_별명_반환() {
        assertThat(service.getNicknames("Cherry MX Brown"))
                .containsExactly("갈축", "체갈");
    }

    @Test
    void 대소문자_구분_없이_매칭() {
        assertThat(service.getNicknames("cherry mx BROWN"))
                .containsExactly("갈축", "체갈");
    }

    @Test
    void 여러_규칙이_동시에_매칭되면_모든_별명을_합쳐_반환() {
        // 이름에 "Brown" 과 "Silent Red" 가 모두 포함되는 경우
        assertThat(service.getNicknames("Brown / Silent Red combo"))
                .containsExactly("갈축", "체갈", "저적", "체리저적");
    }

    @Test
    void 룰이_비어있으면_빈_리스트() {
        ReflectionTestUtils.setField(service, "rules", new ArrayList<>());
        assertThat(service.getNicknames("Cherry MX Brown")).isEmpty();
    }
}
