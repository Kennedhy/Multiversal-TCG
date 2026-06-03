package com.team.multiversaltcg.game.ranking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchHistoryRepository extends JpaRepository<MatchHistory, Long> {

    boolean existsByRoomCode(String roomCode);

    List<MatchHistory> findByCreatorIdOrGuestIdOrderByPlayedAtDesc(String creatorId, String guestId, Pageable pageable);
}
