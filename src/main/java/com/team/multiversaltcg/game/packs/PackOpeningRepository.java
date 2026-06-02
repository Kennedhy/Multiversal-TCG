package com.team.multiversaltcg.game.packs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackOpeningRepository extends JpaRepository<PackOpening, String> {

    List<PackOpening> findTop10ByPlayerIdOrderByCreatedAtDesc(String playerId);
}
