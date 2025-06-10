package com.api.matrimony.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
* Conversation Entity
*/
@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @ManyToOne
   @JoinColumn(name = "user1_id", nullable = false)
   private User user1;

   @ManyToOne
   @JoinColumn(name = "user2_id", nullable = false)
   private User user2;

   @ManyToOne
   @JoinColumn(name = "match_id", nullable = false)
   private Match match;

   @Column(name = "is_active")
   private Boolean isActive = true;

   @Column(name = "created_at")
   private LocalDateTime createdAt = LocalDateTime.now();

   @Column(name = "updated_at")
   private LocalDateTime updatedAt = LocalDateTime.now();

   @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
   private List<Message> messages;

   @PreUpdate
   public void preUpdate() {
       this.updatedAt = LocalDateTime.now();
   }
}

