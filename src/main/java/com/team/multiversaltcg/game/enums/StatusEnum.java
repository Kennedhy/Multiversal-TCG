package com.team.multiversaltcg.game.enums;

public enum StatusEnum {

    QUEIMADO("Queimado", 2, false),
    ENVENENADO("Envenenado", 4, true),
    CONGELADO("Congelado", 1, false),
    CONFUSO("Confuso", 2, false);

    private final String nome;
    private final int duracaoPadrao;
    private final boolean tickACada2Turnos;

    StatusEnum(String nome, int duracaoPadrao, boolean tickACada2Turnos) {
        this.nome = nome;
        this.duracaoPadrao = duracaoPadrao;
        this.tickACada2Turnos = tickACada2Turnos;
    }

    public String getNome() {
        return nome;
    }

    public int getDuracaoPadrao() {
        return duracaoPadrao;
    }

    public boolean isTickACada2Turnos() {
        return tickACada2Turnos;
    }
}
