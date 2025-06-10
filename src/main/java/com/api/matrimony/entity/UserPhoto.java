package com.api.matrimony.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//UserPhoto Entity

@Entity
@Table(name = "user_photos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPhoto {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;

 @ManyToOne
 @JoinColumn(name = "user_id", nullable = false)
 private User user;

 @Column(name = "photo_url", nullable = false, length = 500)
 private String photoUrl;

 @Column(name = "is_primary")
 private Boolean isPrimary = false;

 @Column(name = "display_order")
 private Integer displayOrder = 0;

 @Column(name = "uploaded_at")
 private LocalDateTime uploadedAt = LocalDateTime.now();
}