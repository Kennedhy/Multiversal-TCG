package com.team.multiversaltcg.game.model;

import com.team.multiversaltcg.game.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusAtivo {

    private StatusEnum tipo;
    private int turnosRestantes;
    private int tickContador;

    public boolean expirou() {
        return turnosRestantes <= 0;
    }

    public void decrementar() {
        turnosRestantes--;
    }

    public void incrementarTick() {
        tickContador++;
    }

    public boolean deveAplicarDano() {
        if (!tipo.isTickACada2Turnos()) return true;
        return tickContador % 2 == 0;
    }
}