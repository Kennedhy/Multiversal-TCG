package com.team.multiversaltcg.game.dto;

import com.team.multiversaltcg.game.model.AcaoEfeitoTurno;
import lombok.Data;

@Data
public class EfeitoDTO {

    private int indiceMao;
    private int slotZona;
    private int slotMonstroAlvo;
    private int slotAlvo;
    private boolean alvoInimigo;
    private boolean alvoLider;
    private int escolhaBusca;

    public AcaoEfeitoTurno toAcaoEfeitoTurno() {
        return AcaoEfeitoTurno.builder()
                .indiceMao(indiceMao)
                .slotZona(slotZona)
                .slotMonstroAlvo(slotMonstroAlvo)
                .slotAlvo(slotAlvo)
                .alvoInimigo(alvoInimigo)
                .alvoLider(alvoLider)
                .escolhaBusca(escolhaBusca)
                .build();
    }
}
