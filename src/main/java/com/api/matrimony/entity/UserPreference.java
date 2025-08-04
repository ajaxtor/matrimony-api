package com.api.matrimony.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

//UserPreference Entity

@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPreference {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @OneToOne
 @JoinColumn(name = "user_id", nullable = false)
 private User user;

 @Column(name = "min_age")
 private Integer minAge = 18;

 @Column(name = "max_age")
 private Integer maxAge = 60;

 @Column(name = "min_height")
 private Integer minHeight;

 @Column(name = "max_height")
 private Integer maxHeight;

 @Column(name = "marital_status", length = 100)
 private String maritalStatus; // comma separated

 @Column(length = 100)
 private String religion; // comma separated

 @Column(length = 100)
 private String caste; // comma separated

 @Column(length = 200)
 private String education; // comma separated

 @Column(length = 200)
 private String occupation; // comma separated

 @Column(name = "min_income", precision = 15, scale = 2)
 private java.math.BigDecimal minIncome;

 @Column(name = "max_income", precision = 15, scale = 2)
 private java.math.BigDecimal maxIncome;

 @Column(length = 500)
 private String cities; // comma separated

 @Column(length = 500)
 private String states; // comma separated

 @Column(length = 200)
 private String countries = "India";
 @Column(name = "gender")
 private String gender;
 @Column(name = "sub_caste")
 private String subCaste;
 @Column(name = "mother_tongue")
 private String motherTongue;
 @Column(name = "family_type")
 private String familyType;
 @Column(name = "diet")
 private String diet;

 @Column(name = "created_at")
 private LocalDateTime createdAt = LocalDateTime.now();

 @Column(name = "updated_at")
 private LocalDateTime updatedAt = LocalDateTime.now();

 @PreUpdate
 public void preUpdate() {
     this.updatedAt = LocalDateTime.now();
 }
}