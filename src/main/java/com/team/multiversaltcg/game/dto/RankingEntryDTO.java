package com.team.multiversaltcg.game.dto;

import com.team.multiversaltcg.game.players.PlayerProfile;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RankingEntryDTO {

    private int position;
    private String playerId;
    private int rankingPoints;
    private int matchesPlayed;
    private int wins;
    private int losses;
    private int draws;
    private double winRate;

    public static RankingEntryDTO from(PlayerProfile profile, int position) {
        double winRate = profile.getMatchesPlayed() == 0
                ? 0.0
                : (profile.getWins() * 100.0) / profile.getMatchesPlayed();
        return RankingEntryDTO.builder()
                .position(position)
                .playerId(profile.getPlayerId())
                .rankingPoints(profile.getRankingPoints())
                .matchesPlayed(profile.getMatchesPlayed())
                .wins(profile.getWins())
                .losses(profile.getLosses())
                .draws(profile.getDraws())
                .winRate(Math.round(winRate * 10.0) / 10.0)
                .build();
    }
}
