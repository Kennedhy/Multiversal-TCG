package com.team.multiversaltcg.game.decks;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "player_decks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDeck {

    @Id
    private String id;

    @Column(nullable = false)
    private String playerId;

    @Column(nullable = false)
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String cardsJson;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
