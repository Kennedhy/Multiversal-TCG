package com.team.multiversaltcg.game.decks;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerDeckRepository extends JpaRepository<PlayerDeck, String> {

    List<PlayerDeck> findByPlayerIdOrderByUpdatedAtDesc(String playerId);
}
