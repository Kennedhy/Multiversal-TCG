package com.team.multiversaltcg.game.players;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, String> {

    List<PlayerProfile> findAllByOrderByRankingPointsDescWinsDescMatchesPlayedAsc(Pageable pageable);
}
