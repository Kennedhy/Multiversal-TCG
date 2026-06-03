package com.team.multiversaltcg.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackAdminDTO {

    private String id;
    private String nome;
    private String descricao;
    private String imageUrl;
    private int cost;
    private int cardsPerPack;
    private boolean active;
    private List<String> cardIds;
    private List<CollectionCardDTO> cards;
    private int cardCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
