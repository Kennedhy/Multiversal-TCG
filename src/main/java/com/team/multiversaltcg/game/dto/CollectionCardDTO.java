package com.team.multiversaltcg.game.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class CollectionCardDTO {

    private String id;
    private String nome;
    private String cardType;
    private String rarity;
    private List<String> rarities;
    private Map<String, String> rarityImageUrls;
    private String tipo;
    private String universo;
    private String imageUrl;
    private int copies;
    private boolean owned;
}
