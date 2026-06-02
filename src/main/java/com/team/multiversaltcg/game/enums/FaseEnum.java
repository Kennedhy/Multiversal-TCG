package com.team.multiversaltcg.game.enums;

public enum FaseEnum {

    ATRIBUICAO("Atribuicao de Modos"),
    FARM("Resolucao de Farm"),
    DEFESA("Ativacao de Defesas"),
    ATAQUE("Resolucao de Ataques"),
    STATUS("Ticks de Status"),
    FIM_TURNO("Fim do Turno"),
    FIM_JOGO("Fim de Jogo");

    private final String descricao;

    FaseEnum(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
