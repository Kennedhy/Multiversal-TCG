package com.team.multiversaltcg.game.collections;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerCardCollectionRepository extends JpaRepository<PlayerCardCollection, String> {

    List<PlayerCardCollection> findByPlayerIdOrderByCardIdAsc(String playerId);

    Optional<PlayerCardCollection> findByPlayerIdAndCardId(String playerId, String cardId);
}
