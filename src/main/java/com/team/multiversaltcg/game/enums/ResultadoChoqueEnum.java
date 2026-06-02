package com.team.multiversaltcg.game.enums;

public enum ResultadoChoqueEnum {

    VITORIA("Vitoria"),
    DERROTA("Derrota"),
    EMPATE("Empate");

    private final String descricao;

    ResultadoChoqueEnum(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
