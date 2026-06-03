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
public class PackShopDTO {

    private String id;
    private String nome;
    private String descricao;
    private String imageUrl;
    private int cost;
    private int cardsPerPack;
    private int cardCount;
    private List<CollectionCardDTO> cards;
}
