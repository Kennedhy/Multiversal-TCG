package com.team.multiversaltcg.game.model;

import com.team.multiversaltcg.game.enums.LiderEnum;
import lombok.Data;

@Data
public class Lider {

    private LiderEnum tipo;
    private int hp;
    private int hpMaximo;

    public static Lider criar(LiderEnum tipo) {
        Lider l = new Lider();
        l.tipo = tipo;
        l.hp = 50;
        l.hpMaximo = 50;
        return l;
    }

    public void perderHp(int dano) {
        hp = Math.max(0, hp - dano);
    }

    public boolean isDerrotado() {
        return hp <= 0;
    }

    public int getBonusDef() {
        return tipo == LiderEnum.PELE ? 40 : 25;
    }
}
