package com.thockpick.global.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApiResponse 팩토리 메서드의 현행 동작을 캡처하는 특성화 테스트.
 */
class ApiResponseTest {

    @Test
    void success_데이터포함_응답() {
        ApiResponse<String> response = ApiResponse.success("hello");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("hello");
        assertThat(response.getError()).isNull();
    }

    @Test
    void success_데이터없는_응답() {
        ApiResponse<Void> response = ApiResponse.success();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNull();
        assertThat(response.getError()).isNull();
    }

    @Test
    void error_응답은_코드와_메시지를_담는다() {
        ApiResponse<Void> response = ApiResponse.error("NOT_FOUND", "스위치를 찾을 수 없습니다");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getError()).isNotNull();
        assertThat(response.getError().getCode()).isEqualTo("NOT_FOUND");
        assertThat(response.getError().getMessage()).isEqualTo("스위치를 찾을 수 없습니다");
    }
}
