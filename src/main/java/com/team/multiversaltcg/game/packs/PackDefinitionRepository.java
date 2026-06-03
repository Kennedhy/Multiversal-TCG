package com.team.multiversaltcg.game.packs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackDefinitionRepository extends JpaRepository<PackDefinition, String> {

    List<PackDefinition> findAllByOrderByNomeAsc();

    List<PackDefinition> findByActiveTrueOrderByNomeAsc();
}
