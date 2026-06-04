package com.thockpick.domain.switches;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SwitchType.fromString 의 현행 변환 동작을 캡처하는 특성화 테스트.
 */
class SwitchTypeTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void null_빈문자열_공백이면_null_반환(String value) {
        assertThat(SwitchType.fromString(value)).isNull();
    }

    @ParameterizedTest
    @CsvSource({
            "LINEAR, LINEAR",
            "linear, LINEAR",
            "' Linear ', LINEAR",
            "TACTILE, TACTILE",
            "tactile, TACTILE",
            "CLICKY, CLICKY",
            "clicky, CLICKY"
    })
    void 영문이면_대소문자_공백_무시하고_매칭(String input, SwitchType expected) {
        assertThat(SwitchType.fromString(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "리니어, LINEAR",
            "택타일, TACTILE",
            "클릭, CLICKY",
            "클릭키, CLICKY"
    })
    void 한글이_포함되면_해당_타입으로_매칭(String input, SwitchType expected) {
        assertThat(SwitchType.fromString(input)).isEqualTo(expected);
    }

    @Test
    void 알수없는_값이면_예외_발생() {
        assertThatThrownBy(() -> SwitchType.fromString("optical"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("optical");
    }
}
