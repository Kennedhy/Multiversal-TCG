package com.team.multiversaltcg.game.model;

import com.team.multiversaltcg.game.enums.LiderEnum;
import lombok.Data;

@Data
public class Lider {

    private LiderEnum tipo;
    private int hp;
    private int hpMaximo;
    private boolean especialUsado;
    private boolean forcaDefesaProximoTurno;
    private boolean maoEspecialAtivo;
    private boolean napoleonAtacarDuasVezes;
    private boolean genghisAtacarTodos;

    public static Lider criar(LiderEnum tipo) {
        Lider l = new Lider();
        l.tipo = tipo;
        l.hp = 50;
        l.hpMaximo = 50;
        l.especialUsado = false;
        l.forcaDefesaProximoTurno = false;
        l.maoEspecialAtivo = false;
        l.napoleonAtacarDuasVezes = false;
        l.genghisAtacarTodos = false;
        return l;
    }

    public void perderHp(int dano) {
        hp = Math.max(0, hp - dano);
    }

    public boolean isDerrotado() {
        return hp <= 0;
    }

    public boolean podeUsarEspecial() {
        return !especialUsado;
    }

    public void usarEspecial() {
        especialUsado = true;
    }

    public int getBonusDef() {
        return tipo == LiderEnum.STALIN ? 40 : 25;
    }

    public void limparFlagsEspeciais() {
        maoEspecialAtivo = false;
        napoleonAtacarDuasVezes = false;
        genghisAtacarTodos = false;
    }
}