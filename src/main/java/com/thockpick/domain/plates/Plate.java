package com.thockpick.domain.plates;

import com.thockpick.global.common.BaseEntity;
import com.thockpick.global.enums.Flexibility;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 보강판 엔티티
 */
@Entity
@Table(name = "plates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Plate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlateMaterial material;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PlateType type;

    private Integer price;

    @Column(length = 200)
    private String compatibility;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Flexibility flexibility;

    @Column(length = 50)
    private String soundProfile;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer googleSheetsRow;
}
