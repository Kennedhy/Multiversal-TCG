package com.team.multiversaltcg.game.dto;

import com.team.multiversaltcg.game.players.PlayerProfile;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PlayerProfileDTO {

    private String playerId;
    private int coins;
    private boolean initialBonusGranted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PlayerProfileDTO from(PlayerProfile profile) {
        return PlayerProfileDTO.builder()
                .playerId(profile.getPlayerId())
                .coins(profile.getCoins())
                .initialBonusGranted(profile.isInitialBonusGranted())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
