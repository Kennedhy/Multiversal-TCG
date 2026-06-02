package com.team.multiversaltcg.game.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcaoEfeitoTurno {

    private int indiceMao;
    private int slotZona;
    private int slotMonstroAlvo;
    private int slotAlvo;
    private boolean alvoInimigo;
    private boolean alvoLider;
    private int escolhaBusca;
}
