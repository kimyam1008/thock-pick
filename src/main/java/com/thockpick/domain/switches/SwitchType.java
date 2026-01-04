package com.thockpick.domain.switches;

/**
 * 스위치 타입
 */
public enum SwitchType {
    LINEAR,      // 리니어 (직선형, 부드러운 키감)
    TACTILE,     // 택타일 (촉감 피드백)
    CLICKY;      // 클릭키 (소리 + 촉감 피드백)

    /**
     * 문자열을 SwitchType으로 변환 (대소문자 무시, 한글 지원)
     */
    public static SwitchType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalized = value.trim().toUpperCase();

        // 영문으로 직접 매칭 시도
        for (SwitchType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }

        // 한글 매칭
        if (normalized.contains("리니어") || normalized.equalsIgnoreCase("linear")) {
            return LINEAR;
        } else if (normalized.contains("택타일") || normalized.equalsIgnoreCase("tactile")) {
            return TACTILE;
        } else if (normalized.contains("클릭") || normalized.equalsIgnoreCase("clicky")) {
            return CLICKY;
        }

        throw new IllegalArgumentException("Unknown switch type: " + value);
    }
}
