package com.team.multiversaltcg.game.model;

import com.team.multiversaltcg.game.enums.ModoAcao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcaoTurno {

    private int slotOrigem;
    private ModoAcao modo;
    private int indiceAtaque;
    private int slotAlvo;
    private boolean alvoDiretoLider;

    public boolean isAtaque() {
        return modo == ModoAcao.ATAQUE;
    }

    public boolean isDefesa() {
        return modo == ModoAcao.DEFESA;
    }

    public boolean isFarm() {
        return modo == ModoAcao.FARM;
    }

    public boolean valida(CampoBatalha campo) {
        if (modo == null) return false;
        if (slotOrigem < 0 || slotOrigem > 2) return false;
        if (campo.getSlotsJogador()[slotOrigem] == null) return false;
        if (modo == ModoAcao.ATAQUE) {
            if (indiceAtaque < 0) return false;
            if (alvoDiretoLider) return campo.temSlotVazio(false);
            if (slotAlvo < 0 || slotAlvo > 2) return false;
            if (campo.getSlotsInimigo()[slotAlvo] == null) return false;
        }
        return true;
    }
}
