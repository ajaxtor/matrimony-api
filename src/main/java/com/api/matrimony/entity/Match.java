package com.api.matrimony.entity;

import java.time.LocalDateTime;

import com.api.matrimony.enums.MatchStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 
 @Column(name = "match_id", unique = true, nullable = false)
 private String matchId;

 @ManyToOne
 @JoinColumn(name = "user_id", nullable = false)
 private User user;

 @ManyToOne
 @JoinColumn(name = "matched_user_id", nullable = false)
 private User matchedUser;

 @Enumerated(EnumType.STRING)
 @Column(nullable = false)
 private MatchStatus status = MatchStatus.PENDING;

 @Column(name = "match_score", precision = 5, scale = 2)
 private java.math.BigDecimal matchScore = java.math.BigDecimal.ZERO;

 @Column(name = "matched_at")
 private LocalDateTime matchedAt = LocalDateTime.now();

 @Column(name = "updated_at")
 private LocalDateTime updatedAt = LocalDateTime.now();

 @PreUpdate
 public void preUpdate() {
     this.updatedAt = LocalDateTime.now();
 }
}