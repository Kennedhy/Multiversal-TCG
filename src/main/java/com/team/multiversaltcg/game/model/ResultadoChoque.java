package com.team.multiversaltcg.game.model;

import com.team.multiversaltcg.game.enums.ResultadoChoqueEnum;
import com.team.multiversaltcg.game.enums.StatusEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResultadoChoque {

    private ResultadoChoqueEnum resultado;

    private String nomeAtacante;
    private String nomeDefensor;
    private String idAtacante;
    private String idDefensor;
    private boolean atacanteJogador;

    private int atkEfetivo;
    private int defEfetiva;
    private double multiplicadorTipo;

    private int pressaoAplicadaAtacante;
    private int pressaoAplicadaDefensor;

    private StatusEnum statusAplicado;
    private int duracaoStatus;

    private boolean bloqueioDefesa;
    private int danoLiderBloqueio;

    private int auraAbsorvida;

    private String descricaoLog;

    public boolean foiVitoria() {
        return resultado == ResultadoChoqueEnum.VITORIA;
    }

    public boolean foiDerrota() {
        return resultado == ResultadoChoqueEnum.DERROTA;
    }

    public boolean foiEmpate() {
        return resultado == ResultadoChoqueEnum.EMPATE;
    }
}
