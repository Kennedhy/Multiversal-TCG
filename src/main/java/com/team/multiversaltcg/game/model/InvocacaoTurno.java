package com.team.multiversaltcg.game.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvocacaoTurno {

    private int indiceMao;
    private int slotDestino;
}
