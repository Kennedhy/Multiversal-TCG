package com.team.multiversaltcg.game.dto;

import com.team.multiversaltcg.game.model.InvocacaoTurno;
import lombok.Data;

@Data
public class InvocacaoDTO {

    private int indiceMao;
    private int slotDestino;

    public InvocacaoTurno toInvocacaoTurno() {
        return InvocacaoTurno.builder()
                .indiceMao(indiceMao)
                .slotDestino(slotDestino)
                .build();
    }
}
