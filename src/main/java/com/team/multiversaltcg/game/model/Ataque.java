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
public class Ataque {

    private String nome;
    private int custoAura;
    private int bonusAtk;
    private StatusEnum statusAplicado;
    private int duracaoStatus;

    public boolean temStatus() {
        return statusAplicado != null;
    }

    public int getAtkTotal(int atkBase) {
        return atkBase + bonusAtk;
    }
}