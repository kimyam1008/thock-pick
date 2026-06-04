package com.thockpick.domain.switches;

import com.thockpick.global.enums.SoundProfile;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Switch 엔티티 빌더/업데이트의 현행 동작을 캡처하는 특성화 테스트.
 * 특히 isLubed 의 null 처리 방식이 빌더와 업데이트에서 다르다는 점에 주의.
 */
class SwitchTest {

    @Test
    void 빌더에서_isLubed가_null이면_false로_기본설정() {
        Switch sw = Switch.builder()
                .name("Cherry MX Brown")
                .type(SwitchType.TACTILE)
                .isLubed(null)
                .build();

        assertThat(sw.getIsLubed()).isFalse();
    }

    @Test
    void 빌더에서_isLubed가_true면_true유지() {
        Switch sw = Switch.builder()
                .name("Gateron Oil King")
                .type(SwitchType.LINEAR)
                .isLubed(true)
                .build();

        assertThat(sw.getIsLubed()).isTrue();
    }

    @Test
    void 빌더가_전달한_필드를_그대로_보관한다() {
        Switch sw = Switch.builder()
                .name("Cherry MX Red")
                .type(SwitchType.LINEAR)
                .category("체리")
                .weight(45)
                .manufacturer("Cherry")
                .price(300)
                .actuationForce(45)
                .bottomOutForce(60)
                .travelDistance(new BigDecimal("4.0"))
                .preTravel(new BigDecimal("2.0"))
                .soundProfile(SoundProfile.NORMAL)
                .googleSheetsRow(2)
                .build();

        assertThat(sw.getName()).isEqualTo("Cherry MX Red");
        assertThat(sw.getType()).isEqualTo(SwitchType.LINEAR);
        assertThat(sw.getCategory()).isEqualTo("체리");
        assertThat(sw.getWeight()).isEqualTo(45);
        assertThat(sw.getManufacturer()).isEqualTo("Cherry");
        assertThat(sw.getTravelDistance()).isEqualByComparingTo("4.0");
        assertThat(sw.getSoundProfile()).isEqualTo(SoundProfile.NORMAL);
        assertThat(sw.getGoogleSheetsRow()).isEqualTo(2);
    }

    @Test
    void 빌더는_switchVideos를_빈_리스트로_초기화한다() {
        Switch sw = Switch.builder()
                .name("Cherry MX Brown")
                .type(SwitchType.TACTILE)
                .build();

        assertThat(sw.getSwitchVideos()).isEmpty();
    }

    @Test
    void updateFromGoogleSheets는_전달값을_그대로_덮어쓴다() {
        Switch sw = Switch.builder()
                .name("Old Name")
                .type(SwitchType.LINEAR)
                .isLubed(true)
                .build();

        sw.updateFromGoogleSheets(
                "New Name", SwitchType.CLICKY, "체리", 50, "Cherry",
                500, 50, 65, new BigDecimal("3.5"), new BigDecimal("1.8"),
                "금도금", "POM", "Nylon", SoundProfile.LOUD, false, "설명"
        );

        assertThat(sw.getName()).isEqualTo("New Name");
        assertThat(sw.getType()).isEqualTo(SwitchType.CLICKY);
        assertThat(sw.getIsLubed()).isFalse();
        assertThat(sw.getDescription()).isEqualTo("설명");
    }

    @Test
    void updateFromGoogleSheets는_isLubed에_null을_그대로_허용한다() {
        // 빌더와 달리 업데이트는 null 보정을 하지 않는 현행 동작을 고정.
        Switch sw = Switch.builder()
                .name("Some Switch")
                .type(SwitchType.LINEAR)
                .isLubed(true)
                .build();

        sw.updateFromGoogleSheets(
                "Some Switch", SwitchType.LINEAR, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null, null
        );

        assertThat(sw.getIsLubed()).isNull();
    }
}
