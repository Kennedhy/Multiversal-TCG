package com.team.multiversaltcg.game.collections;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_card_collection")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerCardCollection {

    @Id
    private String id;

    @Column(nullable = false)
    private String playerId;

    @Column(nullable = false)
    private String cardId;

    private int copies;

    public static String idFor(String playerId, String cardId) {
        return playerId + "::" + cardId;
    }
}
