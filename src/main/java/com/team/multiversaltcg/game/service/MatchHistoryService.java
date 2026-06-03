package com.team.multiversaltcg.game.service;

import com.team.multiversaltcg.game.dto.MatchHistoryDTO;
import com.team.multiversaltcg.game.dto.RankingEntryDTO;
import com.team.multiversaltcg.game.players.PlayerProfile;
import com.team.multiversaltcg.game.players.PlayerProfileRepository;
import com.team.multiversaltcg.game.ranking.MatchHistory;
import com.team.multiversaltcg.game.ranking.MatchHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MatchHistoryService {

    private static final int WIN_POINTS = 30;
    private static final int LOSS_POINTS = 15;
    private static final int DRAW_POINTS = 5;

    private final PlayerProfileRepository playerProfileRepository;
    private final MatchHistoryRepository matchHistoryRepository;

    public MatchHistoryService(PlayerProfileRepository playerProfileRepository,
                               MatchHistoryRepository matchHistoryRepository) {
        this.playerProfileRepository = playerProfileRepository;
        this.matchHistoryRepository = matchHistoryRepository;
    }

    @Transactional
    public MatchHistoryDTO recordPvpResult(String roomCode,
                                           String creatorId,
                                           String guestId,
                                           String winnerId,
                                           int turns) {
        if (matchHistoryRepository.existsByRoomCode(roomCode)) {
            return null;
        }

        PlayerProfile creator = getOrCreateProfile(creatorId);
        PlayerProfile guest = getOrCreateProfile(guestId);
        int creatorBefore = creator.getRankingPoints();
        int guestBefore = guest.getRankingPoints();
        boolean draw = winnerId == null || winnerId.isBlank();
        String loserId = draw ? null : winnerId.equalsIgnoreCase(creatorId) ? guestId : creatorId;

        applyResult(creator, draw, winnerId, creatorId);
        applyResult(guest, draw, winnerId, guestId);

        creator = playerProfileRepository.save(creator);
        guest = playerProfileRepository.save(guest);

        MatchHistory match = MatchHistory.builder()
                .roomCode(roomCode)
                .creatorId(creatorId)
                .guestId(guestId)
                .winnerId(draw ? null : winnerId)
                .loserId(loserId)
                .draw(draw)
                .turns(turns)
                .creatorRankingBefore(creatorBefore)
                .creatorRankingAfter(creator.getRankingPoints())
                .guestRankingBefore(guestBefore)
                .guestRankingAfter(guest.getRankingPoints())
                .build();

        return MatchHistoryDTO.from(matchHistoryRepository.save(match), creatorId);
    }

    public List<RankingEntryDTO> ranking(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<PlayerProfile> profiles = playerProfileRepository
                .findAllByOrderByRankingPointsDescWinsDescMatchesPlayedAsc(PageRequest.of(0, safeLimit));
        return java.util.stream.IntStream.range(0, profiles.size())
                .mapToObj(i -> RankingEntryDTO.from(profiles.get(i), i + 1))
                .toList();
    }

    public List<MatchHistoryDTO> historyFor(String playerId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        return matchHistoryRepository
                .findByCreatorIdOrGuestIdOrderByPlayedAtDesc(playerId, playerId, PageRequest.of(0, safeLimit))
                .stream()
                .map(match -> MatchHistoryDTO.from(match, playerId))
                .toList();
    }

    private PlayerProfile getOrCreateProfile(String playerId) {
        return playerProfileRepository.findById(playerId)
                .orElseGet(() -> playerProfileRepository.save(PlayerProfile.builder()
                        .playerId(playerId)
                        .build()));
    }

    private void applyResult(PlayerProfile profile, boolean draw, String winnerId, String playerId) {
        profile.setMatchesPlayed(profile.getMatchesPlayed() + 1);
        if (draw) {
            profile.setDraws(profile.getDraws() + 1);
            profile.setRankingPoints(profile.getRankingPoints() + DRAW_POINTS);
            return;
        }
        if (winnerId.equalsIgnoreCase(playerId)) {
            profile.setWins(profile.getWins() + 1);
            profile.setRankingPoints(profile.getRankingPoints() + WIN_POINTS);
        } else {
            profile.setLosses(profile.getLosses() + 1);
            profile.setRankingPoints(Math.max(0, profile.getRankingPoints() - LOSS_POINTS));
        }
    }
}
