package com.team.multiversaltcg.game.emotes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmoteDefinitionRepository extends JpaRepository<EmoteDefinition, String> {

    List<EmoteDefinition> findAllByOrderByIdAsc();
}