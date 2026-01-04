package com.thockpick.infrastructure.sync.googlesheets.dto;

import com.thockpick.domain.switches.Switch;
import com.thockpick.domain.switches.SwitchType;
import com.thockpick.global.enums.SoundProfile;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Google Sheets의 한 행을 표현하는 DTO
 */
@Slf4j
@Getter
@Builder
public class SwitchSheetRow {
    private Integer rowNumber;          // 행 번호 (Google Sheets 상의 행 번호)
    private String name;                // A: 스위치 이름
    private String type;                // B: 타입 (LINEAR/TACTILE/CLICKY)
    private String category;            // 추가: 시트 탭 이름 (예: 체리, HMX)
    private String weight;              // C: 무게
    private String manufacturer;        // D: 제조사
    private String price;               // E: 가격
    private String actuationForce;      // F: 작동 압력
    private String bottomOutForce;      // G: 바닥 압력
    private String travelDistance;      // H: 총 이동 거리
    private String preTravel;           // I: 작동 거리
    private String springType;          // J: 스프링 타입
    private String stemMaterial;        // K: 스템 재질
    private String housingMaterial;     // L: 하우징 재질
    private String soundProfile;        // M: 소리 특성
    private String isLubed;             // N: 윤활 여부
    private String description;         // O: 설명

    /**
     * Google Sheets API에서 받은 List<Object>를 SwitchSheetRow로 변환 (동적 컬럼 매핑)
     *
     * @param rowNumber  행 번호 (1부터 시작)
     * @param values     Google Sheets의 한 행 데이터
     * @param headerMap  컬럼명과 인덱스가 매핑된 맵
     * @param sheetTitle
     * @return SwitchSheetRow 객체
     */
    public static SwitchSheetRow from(int rowNumber, List<Object> values, Map<String, Integer> headerMap, String sheetTitle) {

        // 키압과 소재는 복합 데이터이므로 먼저 파싱
        String keyPressure = getStringValue(values, headerMap, "키압");
        String materialsRaw = getStringValue(values, headerMap, "소재");

        String[] forces = parseForces(keyPressure);
        String[] materials = parseMaterials(materialsRaw);

        // 시트 이름 정제 (예: "체리(9)" -> "체리", "Outemu(오테뮤)&Gazzew(4)" -> "Outemu(오테뮤)&Gazzew")
        // 괄호 안에 숫자가 있는 패턴만 제거하거나, 단순히 마지막 괄호 덩어리를 제거
        String cleanCategory = sheetTitle.replaceAll("\\([0-9]+\\)$", "").trim();

        return SwitchSheetRow.builder()
                .rowNumber(rowNumber)
                .category(cleanCategory) // 카테고리 저장
                .name(getStringValue(values, headerMap, "스위치이름"))
                .type(getStringValue(values, headerMap, "스위치타입"))
                .actuationForce(forces[0])
                .bottomOutForce(forces[1])
                .springType(getStringValue(values, headerMap, "스프링"))
                .housingMaterial(materials[0])
                .stemMaterial(materials[1])
                .travelDistance(getStringValue(values, headerMap, "총이동거리"))
                .preTravel(getStringValue(values, headerMap, "입력이동거리"))
                // "비윤활 버전 유무"가 'O'면 비윤활 버전이 있다는 뜻. (단순 매핑)
                // 엔티티의 isLubed(윤활되었는가?)와는 의미가 다를 수 있으므로 주의 필요
                .isLubed(getStringValue(values, headerMap, "비윤활버전유무"))

                // 현재 시트 이미지에 안 보이는 항목들은 일단 null 처리하거나 추후 컬럼명 확인 필요
                .manufacturer(getStringValue(values, headerMap, "제조사"))
                .price(getStringValue(values, headerMap, "가격"))
                .soundProfile(getStringValue(values, headerMap, "소리특성"))
                .description(getStringValue(values, headerMap, "설명"))
                .build();
    }

    /**
     * 헤더 맵을 사용하여 값 추출
     */
    private static String getStringValue(List<Object> values, Map<String, Integer> headerMap, String keyword) {
        // 1. 정확히 일치하는 헤더 찾기
        Integer index = headerMap.get(keyword);

        // 2. 없으면 키워드를 포함하는 헤더 찾기 (유연성 확보)
        if (index == null) {
            for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                if (entry.getKey().contains(keyword)) {
                    index = entry.getValue();
                    break;
                }
            }
        }

        if (index == null || values == null || index >= values.size()) {
            return null;
        }

        Object value = values.get(index);
        return value != null ? value.toString().trim() : null;
    }

    // --- 파싱 헬퍼 메서드 추가 ---

    private static String[] parseForces(String value) {
        String actuation = null;
        String bottomOut = null;

        if (value != null && !value.isEmpty()) {
            // "g", "gf" 등 단위 및 공백 제거
            String cleaned = value.replaceAll("[a-zA-Z\\s]", "");

            if (cleaned.contains("/")) {
                // "?/70" 또는 "45/60" 형태
                String[] parts = cleaned.split("/");
                if (parts.length > 0 && !parts[0].equals("?")) actuation = parts[0];
                if (parts.length > 1 && !parts[1].equals("?")) bottomOut = parts[1];
            } else if (!cleaned.equals("?")) {
                // 숫자만 있는 경우 보통 바닥압
                bottomOut = cleaned;
            }
        }
        return new String[]{actuation, bottomOut};
    }

    private static String[] parseMaterials(String value) {
        String housing = null;
        String stem = null;

        if (value != null && !value.isEmpty()) {
            if (value.contains("상하부") || value.contains("스템")) {
                // "상하부: PC&Nylon Mix / 스템: POM" 형태
                // 줄바꿈이나 / 등으로 나뉘어 있을 수 있음. 단순 포함 여부로 체크
                String[] parts = value.split("[\n/]");
                for (String part : parts) {
                    if (part.contains("상하부")) housing = part.replace("상하부", "").replace(":", "").trim();
                    if (part.contains("스템")) stem = part.replace("스템", "").replace(":", "").trim();
                }
            } else if (value.contains("-")) {
                // "PC-Mixed-POM" 형태 -> 보통 마지막이 스템
                int lastHyphen = value.lastIndexOf("-");
                if (lastHyphen > 0) {
                    housing = value.substring(0, lastHyphen).trim();
                    stem = value.substring(lastHyphen + 1).trim();
                } else {
                    housing = value;
                }
            } else {
                housing = value;
            }
        }
        return new String[]{housing, stem};
    }

    /**
     * SwitchSheetRow를 Switch 엔티티로 변환
     */
    public Switch toEntity() {
        return Switch.builder()
                .name(this.name)
                .type(parseType(this.type))
                .category(this.category) // 엔티티 변환 시 포함
                .weight(parseInteger(this.weight))
                .manufacturer(this.manufacturer)
                .price(parseInteger(this.price))
                .actuationForce(parseInteger(this.actuationForce))
                .bottomOutForce(parseInteger(this.bottomOutForce))
                .travelDistance(parseBigDecimal(this.travelDistance))
                .preTravel(parseBigDecimal(this.preTravel))
                .springType(this.springType)
                .stemMaterial(this.stemMaterial)
                .housingMaterial(this.housingMaterial)
                .soundProfile(parseSoundProfile(this.soundProfile))
                .isLubed(parseBoolean(this.isLubed))
                .description(this.description)
                .googleSheetsRow(this.rowNumber)
                .build();
    }

    private static String getStringValue(List<Object> values, int index) {
        if (values == null || index >= values.size()) {
            return null;
        }
        Object value = values.get(index);
        return value != null ? value.toString().trim() : null;
    }

    private static SwitchType parseType(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return SwitchType.fromString(value);
        } catch (IllegalArgumentException e) {
            // 어떤 값이 들어왔는데 변환에 실패했는지 확인
            log.debug("SwitchType 변환 실패. 입력값: '{}'", value);
            return null;
        }
    }

    private static SoundProfile parseSoundProfile(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return SoundProfile.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Integer parseInteger(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            // 숫자가 아닌 문자 제거 (예: "50g" -> "50")
            String cleaned = value.replaceAll("[^0-9-]", "");
            return cleaned.isEmpty() ? null : Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            // 숫자와 소수점만 남기기 (예: "4.0mm" -> "4.0")
            String cleaned = value.replaceAll("[^0-9.]", "");
            return cleaned.isEmpty() ? null : new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Boolean parseBoolean(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        String lower = value.toLowerCase().trim();
        return lower.equals("true") || lower.equals("yes") || lower.equals("o") || 
               lower.equals("1") || lower.equals("윤활");
    }
}
