package com.team.multiversaltcg.game.engine;

import com.team.multiversaltcg.game.enums.LiderEnum;
import com.team.multiversaltcg.game.enums.ModoAcao;
import com.team.multiversaltcg.game.model.CampoBatalha;
import com.team.multiversaltcg.game.model.Lider;
import com.team.multiversaltcg.game.model.MonstroInstancia;
import com.team.multiversaltcg.game.model.ResultadoChoque;

public class GerenciadorAura {

    private static final int AURA_BASE_POR_TURNO = 3;

    public void aplicarAuraBase(CampoBatalha campo) {
        campo.adicionarAura(true, AURA_BASE_POR_TURNO);
        campo.adicionarAura(false, AURA_BASE_POR_TURNO);
    }

    public void resolverFarmJogador(CampoBatalha campo) {
        resolverFarm(campo, true);
    }

    public void resolverFarmInimigo(CampoBatalha campo) {
        resolverFarm(campo, false);
    }

    private void resolverFarm(CampoBatalha campo, boolean jogador) {
        MonstroInstancia[] slots = jogador
                ? campo.getSlotsJogador()
                : campo.getSlotsInimigo();
        Lider lider = jogador
                ? campo.getLiderJogador()
                : campo.getLiderInimigo();

        for (MonstroInstancia monstro : slots) {
            if (monstro == null) continue;
            if (monstro.getModoAtual() != ModoAcao.FARM) continue;

            int auraGerada = monstro.getTipo().getAuraFarm();

            if (lider.getTipo() == LiderEnum.MAO) {
                auraGerada += 2;
            }
            if (campo.getMagiaAtiva(jogador) != null
                    && "campo_sagrado".equals(campo.getMagiaAtiva(jogador).getId())) {
                auraGerada += 1;
            }

            campo.adicionarAura(jogador, auraGerada);
        }
    }

    public void aplicarAbsorcaoChoque(CampoBatalha campo, ResultadoChoque resultado,
                                      boolean jogadorAtacou) {
        if (resultado.getAuraAbsorvida() > 0) {
            int total = resultado.getAuraAbsorvida();
            if (resultado.foiVitoria() && "greymon".equals(resultado.getIdAtacante())) {
                total += 1;
            }
            if (resultado.foiVitoria()
                    && campo.getMagiaAtiva(jogadorAtacou) != null
                    && "tempestade".equals(campo.getMagiaAtiva(jogadorAtacou).getId())) {
                total += 1;
            }
            campo.adicionarAura(jogadorAtacou, total);
        }
    }

    public void aplicarBonusBloqueio(CampoBatalha campo, ResultadoChoque resultado,
                                     boolean jogadorDefendeu) {
        if (resultado.isBloqueioDefesa()) {
            campo.adicionarAura(jogadorDefendeu, 1);

            if (resultado.getDanoLiderBloqueio() > 0) {
                Lider liderInimigo = jogadorDefendeu
                        ? campo.getLiderInimigo()
                        : campo.getLiderJogador();
                liderInimigo.perderHp(resultado.getDanoLiderBloqueio());
            }
        }
    }

    public boolean gastarAura(CampoBatalha campo, boolean jogador, int custo) {
        return campo.gastarAura(jogador, custo);
    }

    public void aplicarFarmMaoTudoAtacaEFarma(CampoBatalha campo) {
        aplicarFarmMaoTudoAtacaEFarma(campo, true);
    }

    public void aplicarFarmMaoTudoAtacaEFarma(CampoBatalha campo, boolean jogador) {
        MonstroInstancia[] slots = jogador ? campo.getSlotsJogador() : campo.getSlotsInimigo();
        for (MonstroInstancia monstro : slots) {
            if (monstro == null) continue;
            int auraGerada = monstro.getTipo().getAuraFarm() + 2;
            if (campo.getMagiaAtiva(jogador) != null
                    && "campo_sagrado".equals(campo.getMagiaAtiva(jogador).getId())) {
                auraGerada += 1;
            }
            campo.adicionarAura(jogador, auraGerada);
        }
    }
}
