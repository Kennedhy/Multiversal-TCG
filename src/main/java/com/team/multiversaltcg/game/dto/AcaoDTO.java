package com.team.multiversaltcg.game.dto;

import com.team.multiversaltcg.game.enums.ModoAcao;
import com.team.multiversaltcg.game.model.AcaoTurno;
import lombok.Data;

@Data
public class AcaoDTO {

    private int slotOrigem;
    private String modo;
    private int indiceAtaque;
    private int slotAlvo;
    private boolean alvoDiretoLider;

    public AcaoTurno toAcaoTurno() {
        if (modo == null || modo.isBlank()) {
            throw new IllegalArgumentException("Modo da acao e obrigatorio.");
        }
        ModoAcao modoAcao = switch (modo.toUpperCase()) {
            case "DEFESA"  -> ModoAcao.DEFESA;
            case "FARM"    -> ModoAcao.FARM;
            default        -> ModoAcao.ATAQUE;
        };

        return AcaoTurno.builder()
                .slotOrigem(slotOrigem)
                .modo(modoAcao)
                .indiceAtaque(indiceAtaque)
                .slotAlvo(slotAlvo)
                .alvoDiretoLider(alvoDiretoLider)
                .build();
    }
}
