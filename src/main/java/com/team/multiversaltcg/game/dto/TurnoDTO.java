package com.team.multiversaltcg.game.dto;

import com.team.multiversaltcg.game.model.TurnoJogador;
import lombok.Data;

import java.util.List;

@Data
public class TurnoDTO {

    private InvocacaoDTO invocacaoMonstro;
    private EfeitoDTO acaoEfeito;
    private List<AcaoDTO> acoesCombate;

    public TurnoJogador toTurnoJogador() {
        return TurnoJogador.builder()
                .invocacaoMonstro(invocacaoMonstro != null
                        ? invocacaoMonstro.toInvocacaoTurno()
                        : null)
                .acaoEfeito(acaoEfeito != null
                        ? acaoEfeito.toAcaoEfeitoTurno()
                        : null)
                .acoesCombate(acoesCombate == null
                        ? List.of()
                        : acoesCombate.stream().map(AcaoDTO::toAcaoTurno).toList())
                .build();
    }
}
