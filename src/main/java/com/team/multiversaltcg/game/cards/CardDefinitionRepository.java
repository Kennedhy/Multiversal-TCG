package com.team.multiversaltcg.game.cards;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardDefinitionRepository extends JpaRepository<CardDefinition, String> {

    List<CardDefinition> findAllByOrderByNomeAsc();

    List<CardDefinition> findByActiveTrueAndDeckCopiesGreaterThanOrderByNomeAsc(int deckCopies);
}
