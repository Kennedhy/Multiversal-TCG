package com.team.multiversaltcg.game.ranking;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_code", nullable = false, unique = true, length = 16)
    private String roomCode;

    @Column(name = "creator_id", nullable = false, length = 64)
    private String creatorId;

    @Column(name = "guest_id", nullable = false, length = 64)
    private String guestId;

    @Column(name = "winner_id", length = 64)
    private String winnerId;

    @Column(name = "loser_id", length = 64)
    private String loserId;

    @Column(name = "draw", nullable = false, columnDefinition = "integer default 0")
    private boolean draw;

    @Column(name = "turns", nullable = false, columnDefinition = "integer default 0")
    private int turns;

    @Column(name = "creator_ranking_before", nullable = false, columnDefinition = "integer default 0")
    private int creatorRankingBefore;

    @Column(name = "creator_ranking_after", nullable = false, columnDefinition = "integer default 0")
    private int creatorRankingAfter;

    @Column(name = "guest_ranking_before", nullable = false, columnDefinition = "integer default 0")
    private int guestRankingBefore;

    @Column(name = "guest_ranking_after", nullable = false, columnDefinition = "integer default 0")
    private int guestRankingAfter;

    @Column(name = "played_at", nullable = false)
    private LocalDateTime playedAt;

    @PrePersist
    public void prePersist() {
        if (playedAt == null) {
            playedAt = LocalDateTime.now();
        }
    }
}
