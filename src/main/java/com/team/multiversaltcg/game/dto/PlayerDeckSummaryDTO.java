package com.team.multiversaltcg.game.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDeckSummaryDTO {

    private String id;
    private String playerId;
    private String name;
    private int total;
}
