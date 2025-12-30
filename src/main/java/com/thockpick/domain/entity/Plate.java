package com.thockpick.domain.entity;

import com.thockpick.domain.enums.Flexibility;
import com.thockpick.domain.enums.PlateMaterial;
import com.thockpick.domain.enums.PlateType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 보강판 엔티티
 */
@Entity
@Table(name = "plates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Plate {

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

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
