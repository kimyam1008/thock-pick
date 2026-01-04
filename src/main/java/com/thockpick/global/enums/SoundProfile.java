package com.thockpick.global.enums;

/**
 * 소리 특성
 */
public enum SoundProfile {
    QUIET,       // 조용함
    NORMAL,      // 보통
    LOUD;         // 시끄러움

    /**
     * 문자열을 SoundProfile로 변환 (대소문자 무시, 한글 지원)
     */
    public static SoundProfile fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalized = value.trim().toUpperCase();

        // 영문으로 직접 매칭 시도
        for (SoundProfile profile : values()) {
            if (profile.name().equals(normalized)) {
                return profile;
            }
        }

        // 한글 매칭
        if (normalized.contains("조용") || normalized.equalsIgnoreCase("quiet")) {
            return QUIET;
        } else if (normalized.contains("보통") || normalized.equalsIgnoreCase("normal")) {
            return NORMAL;
        } else if (normalized.contains("시끄") || normalized.equalsIgnoreCase("loud")) {
            return LOUD;
        }

        throw new IllegalArgumentException("Unknown sound profile: " + value);
    }
}
