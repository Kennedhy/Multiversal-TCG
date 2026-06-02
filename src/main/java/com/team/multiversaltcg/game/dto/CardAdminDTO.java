package com.team.multiversaltcg.game.dto;

import com.team.multiversaltcg.game.model.Ataque;
import com.team.multiversaltcg.game.model.EfeitoRegraDeclarativa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardAdminDTO {

    private String id;
    private String nome;
    private String descricao;
    private String imageUrl;
    private Map<String, String> rarityImageUrls;
    private String cardType;
    private String rarity;
    private List<String> rarities;
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
    private List<Ataque> ataques;
    private List<EfeitoRegraDeclarativa> regras;
    private boolean active;
    private int deckCopies;
    private String source;
}
