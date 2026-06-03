package com.team.multiversaltcg.game.packs;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "pack_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackDefinition {

    @Id
    private String id;

    @Column(nullable = false)
    private String nome;

    @Column(length = 1000)
    private String descricao;

    private String imageUrl;
    private int cost;
    private int cardsPerPack;
    private boolean active;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String cardIdsJson;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
