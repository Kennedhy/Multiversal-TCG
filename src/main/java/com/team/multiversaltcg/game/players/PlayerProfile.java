package com.team.multiversaltcg.game.players;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerProfile {

    @Id
    @Column(name = "player_id", nullable = false, length = 64)
    private String playerId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int coins;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private boolean initialBonusGranted;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
