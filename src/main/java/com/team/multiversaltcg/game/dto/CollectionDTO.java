package com.team.multiversaltcg.game.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CollectionDTO {

    private String playerId;
    private int totalOwned;
    private int uniqueOwned;
    private List<CollectionCardDTO> cards;
}
