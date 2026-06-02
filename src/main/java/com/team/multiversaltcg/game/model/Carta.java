package com.team.multiversaltcg.game.model;

import com.team.multiversaltcg.game.enums.CardType;
import com.team.multiversaltcg.game.enums.CardRarity;
import com.team.multiversaltcg.game.enums.TipoEfeito;
import com.team.multiversaltcg.game.enums.TipoUniversal;
import com.team.multiversaltcg.game.enums.TriggerArmadilha;
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
public class Carta {

    public static final String PLACEHOLDER_IMAGE_URL = "/assets/cards/placeholder.png";
    public static final String CARD_BACK_URL = "/assets/cards/card_back.png";

    private String id;
    private String nome;
    private String descricao;
    private String imageUrl;
    private Map<String, String> rarityImageUrls;
    private CardType cardType;
    private CardRarity rarity;

    private TipoUniversal tipo;
    private String universo;
    private int atk;
    private int def;
    private List<Ataque> ataques;
    private String evolucaoId;

    private TipoEfeito efeito;
    private TriggerArmadilha trigger;
    private int custoAura;
    private int valor;
    private int duracao;
    private int turnosRestantes;
    private TipoUniversal tipoAlvo;
    private String baseMonsterId;
    private String evolvedMonsterId;
    private List<EfeitoRegraDeclarativa> regras;

    public boolean isMonstro() {
        return cardType == CardType.MONSTRO;
    }

    public boolean isMagia() {
        return cardType == CardType.MAGIA;
    }

    public boolean isArmadilha() {
        return cardType == CardType.ARMADILHA;
    }

    public boolean isEvolucao() {
        return cardType == CardType.EVOLUCAO;
    }

    public boolean podeEvoluir() {
        return evolucaoId != null && !evolucaoId.isBlank();
    }

    public Ataque getAtaque(int indice) {
        if (ataques == null || indice < 0 || indice >= ataques.size()) {
            throw new IllegalArgumentException("Indice de ataque invalido: " + indice);
        }
        return ataques.get(indice);
    }

    public String getImageUrlOrPlaceholder() {
        if (rarity != null && rarityImageUrls != null) {
            String rarityImageUrl = rarityImageUrls.get(rarity.name());
            if (rarityImageUrl != null && !rarityImageUrl.isBlank()) {
                return rarityImageUrl;
            }
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            return PLACEHOLDER_IMAGE_URL;
        }
        return imageUrl;
    }

    public Carta copy() {
        return Carta.builder()
                .id(id)
                .nome(nome)
                .descricao(descricao)
                .imageUrl(imageUrl)
                .rarityImageUrls(rarityImageUrls)
                .cardType(cardType)
                .rarity(rarity)
                .tipo(tipo)
                .universo(universo)
                .atk(atk)
                .def(def)
                .ataques(ataques)
                .evolucaoId(evolucaoId)
                .efeito(efeito)
                .trigger(trigger)
                .custoAura(custoAura)
                .valor(valor)
                .duracao(duracao)
                .turnosRestantes(turnosRestantes)
                .tipoAlvo(tipoAlvo)
                .baseMonsterId(baseMonsterId)
                .evolvedMonsterId(evolvedMonsterId)
                .regras(regras)
                .build();
    }
}
