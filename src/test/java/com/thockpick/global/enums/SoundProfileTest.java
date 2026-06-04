package com.thockpick.global.enums;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SoundProfile.fromString 의 현행 변환 동작을 캡처하는 특성화 테스트.
 */
class SoundProfileTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void null_빈문자열_공백이면_null_반환(String value) {
        assertThat(SoundProfile.fromString(value)).isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "QUIET, QUIET",
            "quiet, QUIET",
            "' Normal ', NORMAL",
            "NORMAL, NORMAL",
            "LOUD, LOUD",
            "loud, LOUD"
    })
    void 영문이면_대소문자_공백_무시하고_매칭(String input, SoundProfile expected) {
        assertThat(SoundProfile.fromString(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "조용함, QUIET",
            "보통, NORMAL",
            "시끄러움, LOUD"
    })
    void 한글이_포함되면_해당_프로파일로_매칭(String input, SoundProfile expected) {
        assertThat(SoundProfile.fromString(input)).isEqualTo(expected);
    }

    @Test
    void 알수없는_값이면_예외_발생() {
        assertThatThrownBy(() -> SoundProfile.fromString("silent"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("silent");
    }
}
