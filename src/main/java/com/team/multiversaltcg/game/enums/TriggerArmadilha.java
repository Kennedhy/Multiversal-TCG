package com.team.multiversaltcg.game.enums;

public enum TriggerArmadilha {
    AO_ATAQUE,
    AO_INVOCAR,
    AMBOS,
    INIMIGO_ATACA,
    INIMIGO_INVOCA,
    INIMIGO_USA_VANTAGEM_TIPO,
    SEU_MONSTRO_KO,
    INIMIGO_JOGA_MAGIA;

    public boolean disparaAoAtaque() {
        return this == AO_ATAQUE || this == AMBOS || this == INIMIGO_ATACA;
    }

    public boolean disparaAoInvocar() {
        return this == AO_INVOCAR || this == AMBOS || this == INIMIGO_INVOCA;
    }

    public boolean disparaAoUsarVantagemTipo() {
        return this == INIMIGO_USA_VANTAGEM_TIPO;
    }

    public boolean disparaAoKO() {
        return this == SEU_MONSTRO_KO;
    }

    public boolean disparaAoJogarMagia() {
        return this == INIMIGO_JOGA_MAGIA;
    }
}
