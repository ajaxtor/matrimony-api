package com.api.matrimony.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.api.matrimony.enums.AnnualIncomeRanges;
import com.api.matrimony.enums.BadHabits;
import com.api.matrimony.enums.Gender;
import com.api.matrimony.enums.ManglikStatus;
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

    @Column(name = "full_name", nullable = false, length = 50)
    private String fullName;

    @Column(name = "nick_name", length = 50)
    private String nickName; // ✅ New field

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(name = "height")
    private Integer height; // in cm

    @Column(name = "weight")
    private Integer weight; // in kg

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status")
    private MaritalStatus maritalStatus;

    @Column(name = "religion")
    private String religion;

    @Column(name = "caste")
    private String caste;

    @Column(name = "sub_caste")
    private String subCaste;

    @Column(name = "gothra")
    private String gothra; // ✅ New field

    @Enumerated(EnumType.STRING)
    @Column(name = "manglik_status")
    private ManglikStatus manglikStatus = ManglikStatus.DONT_KNOW; // ✅ New field

    @Column(name = "mother_tongue")
    private String motherTongue;

    @Column(name = "education")
    private String education;

    @Column(name = "occupation")
    private String occupation;

    @Enumerated(EnumType.STRING)
    @Column(name = "annual_income")
    private AnnualIncomeRanges annualIncome;

    @Column(name = "about_me", columnDefinition = "TEXT")
    private String aboutMe;

    @Column(name = "family_type")
    private String familyType;

    @Column(name = "family_value")
    private String familyValue;

    @Column(name = "area")
    private String area; // ✅ New field

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country = "INDIA";

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "diet")
    private String diet;

    @Enumerated(EnumType.STRING)
    @Column(name = "smoking_habits")
    private BadHabits smokingHabits = BadHabits.NO; // ✅ New field

    @Enumerated(EnumType.STRING)
    @Column(name = "drinking_habits")
    private BadHabits drinkingHabits = BadHabits.NO; // ✅ New field

    @Column(name = "is_hide")
    private Boolean isHide = false;

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
