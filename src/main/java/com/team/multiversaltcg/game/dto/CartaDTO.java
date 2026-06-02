package com.team.multiversaltcg.game.dto;

import com.team.multiversaltcg.game.model.Carta;
import com.team.multiversaltcg.game.model.ZonaEfeito;
import com.team.multiversaltcg.game.enums.EfeitoAlvo;
import com.team.multiversaltcg.game.enums.EfeitoTrigger;
import com.team.multiversaltcg.game.enums.TipoEfeito;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartaDTO {

    private String id;
    private String nome;
    private String cardType;
    private String rarity;
    private String imageUrl;
    private String cardBackUrl;
    private boolean oculta;
    private String descricao;
    private String tipo;
    private String universo;
    private int atk;
    private int def;
    private String trigger;
    private String efeito;
    private int custoAura;
    private int valor;
    private int duracao;
    private int turnosRestantes;
    private String tipoAlvo;
    private String baseMonsterId;
    private String evolvedMonsterId;
    private String targetingMode;
    private List<AtaqueDTO> ataques;

    public static CartaDTO from(Carta carta) {
        return from(carta, false);
    }

    public static CartaDTO hidden() {
        return CartaDTO.builder()
                .nome("Carta oculta")
                .imageUrl(Carta.CARD_BACK_URL)
                .cardBackUrl(Carta.CARD_BACK_URL)
                .oculta(true)
                .build();
    }

    public static CartaDTO fromZona(ZonaEfeito zona, boolean esconderOculta) {
        if (zona == null || zona.getCarta() == null) return null;
        if (esconderOculta && zona.isViradaParaBaixo()) {
            return hidden();
        }
        CartaDTO dto = from(zona.getCarta(), false);
        dto.setOculta(zona.isViradaParaBaixo());
        return dto;
    }

    public static CartaDTO from(Carta carta, boolean oculta) {
        if (carta == null) return null;
        if (oculta) return hidden();

        List<AtaqueDTO> ataques = carta.getAtaques() == null
                ? List.of()
                : carta.getAtaques().stream().map(AtaqueDTO::from).toList();

        return CartaDTO.builder()
                .id(carta.getId())
                .nome(carta.getNome())
                .cardType(carta.getCardType().name())
                .rarity(carta.getRarity() == null ? null : carta.getRarity().name())
                .imageUrl(carta.getImageUrlOrPlaceholder())
                .cardBackUrl(Carta.CARD_BACK_URL)
                .oculta(false)
                .descricao(carta.getDescricao())
                .tipo(carta.getTipo() != null ? carta.getTipo().getNome() : null)
                .universo(carta.getUniverso())
                .atk(carta.getAtk())
                .def(carta.getDef())
                .trigger(carta.getTrigger() != null ? carta.getTrigger().name() : null)
                .efeito(carta.getEfeito() != null ? carta.getEfeito().name() : null)
                .custoAura(carta.getCustoAura())
                .valor(carta.getValor())
                .duracao(carta.getDuracao())
                .turnosRestantes(carta.getTurnosRestantes())
                .tipoAlvo(carta.getTipoAlvo() != null ? carta.getTipoAlvo().getNome() : null)
                .baseMonsterId(carta.getBaseMonsterId())
                .evolvedMonsterId(carta.getEvolvedMonsterId())
                .targetingMode(targetingMode(carta))
                .ataques(ataques)
                .build();
    }

    private static String targetingMode(Carta carta) {
        if (carta.getEfeito() == TipoEfeito.BUFF_ATK
                || carta.getEfeito() == TipoEfeito.BUFF_DEF
                || carta.getEfeito() == TipoEfeito.CURA_STATUS
                || carta.getEfeito() == TipoEfeito.IMUNIDADE_STATUS) {
            return "OWN";
        }
        if (carta.getEfeito() == TipoEfeito.PRESSAO_ALVO
                || carta.getEfeito() == TipoEfeito.IGNORAR_DEFESA) {
            return "ENEMY";
        }
        if (carta.getRegras() != null) {
            for (var regra : carta.getRegras()) {
                if (regra.getTrigger() != EfeitoTrigger.AO_JOGAR) continue;
                if (regra.getTarget() == EfeitoAlvo.ALLY_TARGET) return "OWN";
                if (regra.getTarget() == EfeitoAlvo.ENEMY_TARGET) return "ENEMY";
            }
        }
        return "NONE";
    }
}
