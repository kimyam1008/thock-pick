package com.thockpick.presentation.dto;

import com.thockpick.domain.switches.Switch;
import com.thockpick.domain.switches.SwitchType;
import lombok.Builder;
import lombok.Getter;

/**
 * 스위치 목록 응답 DTO
 * (불필요한 연관관계인 switchVideos를 제외하여 N+1 문제를 방지)
 */
@Getter
@Builder
public class SwitchListResponse {
    private Long id;
    private String name;
    private SwitchType type;
    private String category;
    private String manufacturer;
    private Integer price;
    private Integer actuationForce;
    private Integer bottomOutForce;

    // 엔티티 -> DTO 변환 메서드
    public static SwitchListResponse from(Switch switchEntity) {
        return SwitchListResponse.builder()
                .id(switchEntity.getId())
                .name(switchEntity.getName())
                .type(switchEntity.getType())
                .category(switchEntity.getCategory())
                .manufacturer(switchEntity.getManufacturer())
                .price(switchEntity.getPrice())
                .actuationForce(switchEntity.getActuationForce())
                .bottomOutForce(switchEntity.getBottomOutForce())
                .build();
    }
}