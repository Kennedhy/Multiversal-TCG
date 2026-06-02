package com.team.multiversaltcg.game.dto;

import com.team.multiversaltcg.game.model.Ataque;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AtaqueDTO {

    private String nome;
    private int custoAura;
    private int bonusAtk;
    private String efeito;

    public static AtaqueDTO from(Ataque ataque) {
        String efeito = "";
        if (ataque.temStatus()) {
            efeito = ataque.getStatusAplicado().getNome()
                    + " (" + ataque.getDuracaoStatus() + "t)";
        }

        return AtaqueDTO.builder()
                .nome(ataque.getNome())
                .custoAura(ataque.getCustoAura())
                .bonusAtk(ataque.getBonusAtk())
                .efeito(efeito)
                .build();
    }
}
