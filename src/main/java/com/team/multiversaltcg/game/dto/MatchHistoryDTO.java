package com.team.multiversaltcg.game.dto;

import com.team.multiversaltcg.game.ranking.MatchHistory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MatchHistoryDTO {

    private Long id;
    private String roomCode;
    private String creatorId;
    private String guestId;
    private String winnerId;
    private String loserId;
    private boolean draw;
    private int turns;
    private int rankingBefore;
    private int rankingAfter;
    private int rankingDelta;
    private LocalDateTime playedAt;

    public static MatchHistoryDTO from(MatchHistory match, String playerId) {
        boolean creator = match.getCreatorId().equalsIgnoreCase(playerId);
        int before = creator ? match.getCreatorRankingBefore() : match.getGuestRankingBefore();
        int after = creator ? match.getCreatorRankingAfter() : match.getGuestRankingAfter();
        return MatchHistoryDTO.builder()
                .id(match.getId())
                .roomCode(match.getRoomCode())
                .creatorId(match.getCreatorId())
                .guestId(match.getGuestId())
                .winnerId(match.getWinnerId())
                .loserId(match.getLoserId())
                .draw(match.isDraw())
                .turns(match.getTurns())
                .rankingBefore(before)
                .rankingAfter(after)
                .rankingDelta(after - before)
                .playedAt(match.getPlayedAt())
                .build();
    }
}
