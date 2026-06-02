package com.team.multiversaltcg.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDeckDTO {

    private String id;
    private String playerId;
    private String name;
    private int total;
    private List<DeckEntryDTO> cards;
}
