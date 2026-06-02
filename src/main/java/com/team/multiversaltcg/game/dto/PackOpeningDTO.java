package com.team.multiversaltcg.game.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PackOpeningDTO {

    private String id;
    private String playerId;
    private int cost;
    private int coinsRemaining;
    private LocalDateTime createdAt;
    private List<CollectionCardDTO> cards;
}
