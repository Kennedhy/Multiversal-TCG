package com.team.multiversaltcg.game.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnoJogador {

    private InvocacaoTurno invocacaoMonstro;
    private AcaoEfeitoTurno acaoEfeito;
    private List<AcaoTurno> acoesCombate;

    public List<AcaoTurno> getAcoesCombate() {
        if (acoesCombate == null) {
            acoesCombate = new ArrayList<>();
        }
        return acoesCombate;
    }
}
