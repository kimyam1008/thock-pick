package com.thockpick.infrastructure.sync.googlesheets.dto;

import com.thockpick.domain.switches.Switch;
import com.thockpick.domain.switches.SwitchType;
import com.thockpick.global.enums.SoundProfile;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SwitchSheetRow 의 파싱/매핑 로직 현행 동작을 캡처하는 특성화 테스트.
 * 시트 원본 문자열의 다양한 형태를 어떻게 정제·변환하는지 고정한다.
 */
class SwitchSheetRowTest {

    /**
     * toEntity() 가 호출하는 숫자/불리언/enum 파서들의 동작.
     * SwitchSheetRow 는 @Builder 라 원본 문자열 필드를 직접 세팅해 검증한다.
     */
    @Nested
    class ToEntity {

        @ParameterizedTest
        @CsvSource({
                "'50', 50",
                "'50g', 50",
                "'45gf', 45",
                "'-5', -5",
                "'', ",
                "'abc', "
        })
        void price_등_정수필드는_숫자만_추출해_파싱(String raw, Integer expected) {
            Switch entity = SwitchSheetRow.builder()
                    .name("x").type("LINEAR").price(raw)
                    .build()
                    .toEntity();

            assertThat(entity.getPrice()).isEqualTo(expected);
        }

        @Test
        void travelDistance는_숫자와_소수점만_추출해_BigDecimal로_파싱() {
            Switch entity = SwitchSheetRow.builder()
                    .name("x").type("LINEAR").travelDistance("4.0mm")
                    .build()
                    .toEntity();

            assertThat(entity.getTravelDistance()).isEqualByComparingTo("4.0");
        }

        @ParameterizedTest
        @CsvSource({
                "true, true",
                "TRUE, true",
                "yes, true",
                "O, true",
                "o, true",
                "1, true",
                "윤활, true",
                "x, false",
                "no, false"
        })
        void isLubed는_지정된_표현만_true로_파싱(String raw, boolean expected) {
            Switch entity = SwitchSheetRow.builder()
                    .name("x").type("LINEAR").isLubed(raw)
                    .build()
                    .toEntity();

            assertThat(entity.getIsLubed()).isEqualTo(expected);
        }

        @Test
        void isLubed가_null이면_false() {
            Switch entity = SwitchSheetRow.builder()
                    .name("x").type("LINEAR").isLubed(null)
                    .build()
                    .toEntity();

            assertThat(entity.getIsLubed()).isFalse();
        }

        @Test
        void type과_soundProfile은_변환실패시_null() {
            Switch entity = SwitchSheetRow.builder()
                    .name("x").type("이상한타입").soundProfile("이상한소리")
                    .build()
                    .toEntity();

            assertThat(entity.getType()).isNull();
            assertThat(entity.getSoundProfile()).isNull();
        }

        @Test
        void 정상_type과_soundProfile은_enum으로_변환() {
            Switch entity = SwitchSheetRow.builder()
                    .name("x").type("리니어").soundProfile("조용함")
                    .build()
                    .toEntity();

            assertThat(entity.getType()).isEqualTo(SwitchType.LINEAR);
            assertThat(entity.getSoundProfile()).isEqualTo(SoundProfile.QUIET);
        }

        @Test
        void rowNumber는_googleSheetsRow로_옮겨진다() {
            Switch entity = SwitchSheetRow.builder()
                    .rowNumber(7).name("x").type("LINEAR")
                    .build()
                    .toEntity();

            assertThat(entity.getGoogleSheetsRow()).isEqualTo(7);
        }
    }

    /**
     * from() 정적 팩토리: 헤더 매핑, 시트명/이름 정제, 키압·소재 복합 파싱.
     */
    @Nested
    class From {

        // 헤더명 -> 인덱스. parseForces/parseMaterials 검증을 위해 키압/소재 포함.
        private final Map<String, Integer> headerMap = Map.of(
                "스위치이름", 0,
                "스위치타입", 1,
                "키압", 2,
                "소재", 3,
                "제조사", 4
        );

        private List<Object> row(Object... values) {
            return Arrays.asList(values);
        }

        @Test
        void 시트명의_끝_괄호숫자는_카테고리에서_제거() {
            SwitchSheetRow result = SwitchSheetRow.from(
                    2, row("Cherry MX Red", "리니어", "45/60", "PC-POM", "Cherry"),
                    headerMap, "체리(9)");

            assertThat(result.getCategory()).isEqualTo("체리");
        }

        @Test
        void 시트명_중간_괄호는_유지하고_끝_괄호숫자만_제거() {
            SwitchSheetRow result = SwitchSheetRow.from(
                    2, row("x", "리니어", "", "", ""),
                    headerMap, "Outemu(오테뮤)&Gazzew(4)");

            assertThat(result.getCategory()).isEqualTo("Outemu(오테뮤)&Gazzew");
        }

        @Test
        void 이름의_물음표는_제거되고_trim된다() {
            SwitchSheetRow result = SwitchSheetRow.from(
                    2, row("  Cherry? MX ", "리니어", "", "", ""),
                    headerMap, "체리(9)");

            assertThat(result.getName()).isEqualTo("Cherry MX");
        }

        @Test
        void 키압_슬래시형식은_작동압_바닥압으로_분리() {
            SwitchSheetRow result = SwitchSheetRow.from(
                    2, row("x", "리니어", "45/60g", "", ""),
                    headerMap, "체리(9)");

            assertThat(result.getActuationForce()).isEqualTo("45");
            assertThat(result.getBottomOutForce()).isEqualTo("60");
        }

        @Test
        void 키압_작동압이_물음표면_null이고_바닥압만_채운다() {
            SwitchSheetRow result = SwitchSheetRow.from(
                    2, row("x", "리니어", "?/70", "", ""),
                    headerMap, "체리(9)");

            assertThat(result.getActuationForce()).isNull();
            assertThat(result.getBottomOutForce()).isEqualTo("70");
        }

        @Test
        void 키압_단일숫자는_바닥압으로_본다() {
            SwitchSheetRow result = SwitchSheetRow.from(
                    2, row("x", "리니어", "60g", "", ""),
                    headerMap, "체리(9)");

            assertThat(result.getActuationForce()).isNull();
            assertThat(result.getBottomOutForce()).isEqualTo("60");
        }

        @Test
        void 소재_상하부스템_라벨형식_분리() {
            SwitchSheetRow result = SwitchSheetRow.from(
                    2, row("x", "리니어", "", "상하부: PC&Nylon Mix / 스템: POM", ""),
                    headerMap, "체리(9)");

            assertThat(result.getHousingMaterial()).isEqualTo("PC&Nylon Mix");
            assertThat(result.getStemMaterial()).isEqualTo("POM");
        }

        @Test
        void 소재_하이픈형식은_마지막을_스템으로_본다() {
            SwitchSheetRow result = SwitchSheetRow.from(
                    2, row("x", "리니어", "", "PC-Mixed-POM", ""),
                    headerMap, "체리(9)");

            assertThat(result.getHousingMaterial()).isEqualTo("PC-Mixed");
            assertThat(result.getStemMaterial()).isEqualTo("POM");
        }

        @Test
        void 소재_단일값은_하우징으로만_채운다() {
            SwitchSheetRow result = SwitchSheetRow.from(
                    2, row("x", "리니어", "", "POM", ""),
                    headerMap, "체리(9)");

            assertThat(result.getHousingMaterial()).isEqualTo("POM");
            assertThat(result.getStemMaterial()).isNull();
        }

        @Test
        void 헤더에_없는_값과_범위초과_인덱스는_null() {
            // 소리특성/설명 헤더가 headerMap 에 없으므로 null 이어야 한다.
            SwitchSheetRow result = SwitchSheetRow.from(
                    2, row("x", "리니어", "", "", ""),
                    headerMap, "체리(9)");

            assertThat(result.getSoundProfile()).isNull();
            assertThat(result.getDescription()).isNull();
        }

        @Test
        void 헤더는_정확매칭이_없으면_키워드_포함으로_찾는다() {
            // "제조사명" 헤더는 "제조사" 를 포함하므로 매칭되어야 한다.
            Map<String, Integer> partialHeader = Map.of(
                    "스위치이름", 0, "스위치타입", 1, "제조사명", 2);

            SwitchSheetRow result = SwitchSheetRow.from(
                    2, row("x", "리니어", "Cherry"),
                    partialHeader, "체리(9)");

            assertThat(result.getManufacturer()).isEqualTo("Cherry");
        }
    }
}
