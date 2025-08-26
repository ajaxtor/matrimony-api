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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "matches_action")

public class MatchesAction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(name = "match_id", nullable = false)
    private String matchId;

    @ManyToOne
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MatchStatus status = MatchStatus.PENDING;

    @Column(name = "send_action_time", nullable = false)
    private LocalDateTime sendActionTime = LocalDateTime.now();

    @Column(name = "update_action_time", nullable = false)
    private LocalDateTime updateActionTime = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updateActionTime = LocalDateTime.now();
    }
}
