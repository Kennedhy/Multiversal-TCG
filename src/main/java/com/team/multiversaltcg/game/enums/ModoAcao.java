package com.team.multiversaltcg.game.enums;

public enum ModoAcao {

    ATAQUE("Ataque", false),
    DEFESA("Defesa", true),
    FARM("Farm", true);

    private final String nome;
    private final boolean bloqueiaAtaque;

    ModoAcao(String nome, boolean bloqueiaAtaque) {
        this.nome = nome;
        this.bloqueiaAtaque = bloqueiaAtaque;
    }

    public String getNome() {
        return nome;
    }

    public boolean isBloqueiaAtaque() {
        return bloqueiaAtaque;
    }
}