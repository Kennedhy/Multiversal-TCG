package com.team.multiversaltcg.game.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ShopDTO {

    private String playerId;
    private int coins;
    private List<PackShopDTO> packs;
    private Map<String, Integer> odds;
}
