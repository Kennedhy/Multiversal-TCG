package com.team.multiversaltcg.game.cards;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "card_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDefinition {

    @Id
    private String id;

    @Column(nullable = false)
    private String nome;

    @Column(length = 1000)
    private String descricao;

    private String imageUrl;

    @Column(nullable = false)
    private String cardType;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String rarityImageUrlsJson;

    private String rarity;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String raritiesJson;

    private String tipo;
    private String universo;
    private int atk;
    private int def;
    private String evolucaoId;

    private String efeito;
    private String trigger;
    private int custoAura;
    private int valor;
    private int duracao;
    private String tipoAlvo;
    private String baseMonsterId;
    private String evolvedMonsterId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String ataquesJson;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String regrasJson;

    private boolean active;
    private int deckCopies;
    private String source;
}
