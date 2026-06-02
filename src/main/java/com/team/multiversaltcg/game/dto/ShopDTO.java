package com.team.multiversaltcg.game.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ShopDTO {

    private String playerId;
    private int coins;
    private int packCost;
    private int cardsPerPack;
    private Map<String, Integer> odds;
}
