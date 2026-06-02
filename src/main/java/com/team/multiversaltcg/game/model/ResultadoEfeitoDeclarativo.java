package com.team.multiversaltcg.game.model;

public record ResultadoEfeitoDeclarativo(boolean cancelarAtaque, boolean neutralizarTipo) {

    public static ResultadoEfeitoDeclarativo nenhum() {
        return new ResultadoEfeitoDeclarativo(false, false);
    }

    public ResultadoEfeitoDeclarativo merge(ResultadoEfeitoDeclarativo outro) {
        if (outro == null) return this;
        return new ResultadoEfeitoDeclarativo(
                cancelarAtaque || outro.cancelarAtaque(),
                neutralizarTipo || outro.neutralizarTipo()
        );
    }
}
