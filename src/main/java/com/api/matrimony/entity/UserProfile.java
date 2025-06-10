package com.api.matrimony.entity;

import java.time.LocalDateTime;

import com.api.matrimony.enums.Gender;
import com.api.matrimony.enums.MaritalStatus;

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

//UserProfile Entity
@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @OneToOne
 @JoinColumn(name = "user_id", nullable = false)
 private User user;

 @Column(name = "first_name", nullable = false, length = 50)
 private String firstName;

 @Column(name = "last_name", nullable = false, length = 50)
 private String lastName;

 @Column(name = "date_of_birth", nullable = false)
 private java.time.LocalDate dateOfBirth;

 @Enumerated(EnumType.STRING)
 @Column(nullable = false)
 private Gender gender;

 private Integer height; // in cm

 private Integer weight; // in kg

 @Enumerated(EnumType.STRING)
 @Column(name = "marital_status")
 private MaritalStatus maritalStatus;

 private String religion;

 private String caste;

 @Column(name = "sub_caste")
 private String subCaste;

 @Column(name = "mother_tongue")
 private String motherTongue;

 private String education;

 private String occupation;

 @Column(name = "annual_income", precision = 15, scale = 2)
 private java.math.BigDecimal annualIncome;

 @Column(name = "about_me", columnDefinition = "TEXT")
 private String aboutMe;

 @Column(name = "family_type")
 private String familyType;

 @Column(name = "family_values")
 private String familyValues;

 private String city;

 private String state;

 private String country = "India";

 private String pincode;

 @Column(name = "profile_created_by")
 private String profileCreatedBy;

 @Column(name = "created_at")
 private LocalDateTime createdAt = LocalDateTime.now();

 @Column(name = "updated_at")
 private LocalDateTime updatedAt = LocalDateTime.now();

 @PreUpdate
 public void preUpdate() {
     this.updatedAt = LocalDateTime.now();
 }
}
