package com.api.matrimony.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.api.matrimony.enums.ManglikStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kundli Data Entity
 */

@Entity
@Table(name = "kundli_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KundliData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "birth_time")
    private LocalTime birthTime;

    @Column(name = "birth_place", length = 200)
    private String birthPlace;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(length = 50)
    private String rashi;

    @Column(length = 50)
    private String nakshatra;

    @Column(length = 50)
    private String gotra;

    @Enumerated(EnumType.STRING)
    @Column(name = "manglik_status")
    private ManglikStatus manglikStatus = ManglikStatus.UNKNOWN;

    @Column(name = "kundli_details", columnDefinition = "JSON")
    private String kundliDetails;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

