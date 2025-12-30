package com.thockpick.domain.entity;

import com.thockpick.domain.enums.SoundProfile;
import com.thockpick.domain.enums.SwitchType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@EntityListeners(AuditingEntityListener.class)
public class Switch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SwitchType type;

    private Integer weight;

    @Column(length = 50)
    private String manufacturer;

    private Integer price;

    private Integer actuationForce;

    private Integer bottomOutForce;

    @Column(precision = 3, scale = 1)
    private BigDecimal travelDistance;

    @Column(precision = 3, scale = 1)
    private BigDecimal preTravel;

    @Column(length = 50)
    private String springType;

    @Column(length = 50)
    private String stemMaterial;

    @Column(length = 50)
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

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
