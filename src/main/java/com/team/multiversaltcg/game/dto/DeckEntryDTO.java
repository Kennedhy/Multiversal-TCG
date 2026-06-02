package com.team.multiversaltcg.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckEntryDTO {

    private String id;
    private String nome;
    private String cardType;
    private String rarity;
    private String imageUrl;
    private boolean active;
    private int deckCopies;
}
