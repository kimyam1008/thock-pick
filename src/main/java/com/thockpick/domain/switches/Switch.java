package com.thockpick.domain.switches;

import com.thockpick.global.common.BaseEntity;
import com.thockpick.global.enums.SoundProfile;
import com.thockpick.domain.videos.SwitchVideo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 스위치 엔티티
 */
@Entity
@Table(name = "switches", indexes = {
        @Index(name = "idx_switch_type", columnList = "type"),
        @Index(name = "idx_switch_manufacturer", columnList = "manufacturer"),
        @Index(name = "idx_switch_price", columnList = "price"),
        @Index(name = "idx_switch_name", columnList = "name"),
        @Index(name = "idx_google_sheets_row", columnList = "googleSheetsRow")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Switch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SwitchType type;

    @Column(length = 50)
    private String category; // 탭 정보 저장 (예: 체리, 저소음, HMX 등)

    private Integer weight;

    @Column(length = 255)
    private String manufacturer;

    private Integer price;

    private Integer actuationForce;

    private Integer bottomOutForce;

    @Column(precision = 3, scale = 1)
    private BigDecimal travelDistance;

    @Column(precision = 3, scale = 1)
    private BigDecimal preTravel;

    @Column(length = 255)
    private String springType;

    @Column(length = 255)
    private String stemMaterial;

    @Column(length = 255)
    private String housingMaterial;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SoundProfile soundProfile;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isLubed;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer googleSheetsRow;

    @OneToMany(mappedBy = "switchEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SwitchVideo> switchVideos = new ArrayList<>();

    @Builder
    public Switch(String name, SwitchType type, String category, Integer weight, String manufacturer,
                  Integer price, Integer actuationForce, Integer bottomOutForce,
                  BigDecimal travelDistance, BigDecimal preTravel, String springType,
                  String stemMaterial, String housingMaterial, SoundProfile soundProfile,
                  Boolean isLubed, String description, Integer googleSheetsRow) {
        this.name = name;
        this.type = type;
        this.category = category;
        this.weight = weight;
        this.manufacturer = manufacturer;
        this.price = price;
        this.actuationForce = actuationForce;
        this.bottomOutForce = bottomOutForce;
        this.travelDistance = travelDistance;
        this.preTravel = preTravel;
        this.springType = springType;
        this.stemMaterial = stemMaterial;
        this.housingMaterial = housingMaterial;
        this.soundProfile = soundProfile;
        this.isLubed = isLubed != null ? isLubed : false;
        this.description = description;
        this.googleSheetsRow = googleSheetsRow;
    }

    /**
     * Google Sheets 데이터로 엔티티 업데이트
     */
    public void updateFromGoogleSheets(String name, SwitchType type, String category, Integer weight,
                                       String manufacturer, Integer price, Integer actuationForce,
                                       Integer bottomOutForce, BigDecimal travelDistance,
                                       BigDecimal preTravel, String springType, String stemMaterial,
                                       String housingMaterial, SoundProfile soundProfile,
                                       Boolean isLubed, String description) {
        this.name = name;
        this.type = type;
        this.category = category;
        this.weight = weight;
        this.manufacturer = manufacturer;
        this.price = price;
        this.actuationForce = actuationForce;
        this.bottomOutForce = bottomOutForce;
        this.travelDistance = travelDistance;
        this.preTravel = preTravel;
        this.springType = springType;
        this.stemMaterial = stemMaterial;
        this.housingMaterial = housingMaterial;
        this.soundProfile = soundProfile;
        this.isLubed = isLubed;
        this.description = description;
    }
}
