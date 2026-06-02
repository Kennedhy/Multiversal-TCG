package com.team.multiversaltcg.game.engine;

import com.team.multiversaltcg.game.enums.LiderEnum;
import com.team.multiversaltcg.game.enums.ModoAcao;
import com.team.multiversaltcg.game.model.CampoBatalha;
import com.team.multiversaltcg.game.model.Lider;
import com.team.multiversaltcg.game.model.MonstroInstancia;

import java.util.ArrayList;
import java.util.List;

public class GerenciadorLider {

    public void ativarEspecial(CampoBatalha campo, boolean jogador) {
        Lider lider = jogador ? campo.getLiderJogador() : campo.getLiderInimigo();
        if (!lider.podeUsarEspecial()) return;
        lider.usarEspecial();

        switch (lider.getTipo()) {
            case MAO     -> ativarEspecialMao(campo, jogador, lider);
            case KIM     -> ativarEspecialKim(campo, jogador);
            case STALIN  -> ativarEspecialStalin(campo, jogador);
            case NAPOLEON -> ativarEspecialNapoleon(lider);
            case GENGHIS -> ativarEspecialGenghis(lider);
        }
    }

    private void ativarEspecialMao(CampoBatalha campo, boolean jogador, Lider lider) {
        lider.setMaoEspecialAtivo(true);
    }

    private void ativarEspecialKim(CampoBatalha campo, boolean jogador) {
        MonstroInstancia[] slotsInimigos = jogador
                ? campo.getSlotsInimigo()
                : campo.getSlotsJogador();
        for (MonstroInstancia m : slotsInimigos) {
            if (m != null) {
                m.setModoAtual(ModoAcao.DEFESA);
                m.setTurnosPressoDef(1);
            }
        }
    }

    private void ativarEspecialStalin(CampoBatalha campo, boolean jogador) {
        MonstroInstancia[] slotsInimigos = jogador
                ? campo.getSlotsInimigo()
                : campo.getSlotsJogador();
        for (MonstroInstancia m : slotsInimigos) {
            if (m != null) {
                m.setModoAtual(ModoAcao.DEFESA);
                m.setTurnosPressoDef(2);
                break;
            }
        }
    }

    private void ativarEspecialNapoleon(Lider lider) {
        lider.setNapoleonAtacarDuasVezes(true);
    }

    private void ativarEspecialGenghis(Lider lider) {
        lider.setGenghisAtacarTodos(true);
    }

    public boolean verificarNapoleonBonus(CampoBatalha campo, boolean jogador) {
        MonstroInstancia[] slots = jogador
                ? campo.getSlotsJogador()
                : campo.getSlotsInimigo();
        for (MonstroInstancia m : slots) {
            if (m != null && m.getModoAtual() != ModoAcao.ATAQUE) return false;
        }
        return true;
    }

    public void aplicarPassivaGulag(CampoBatalha campo, boolean jogadorComStalin) {
        Lider lider = jogadorComStalin
                ? campo.getLiderJogador()
                : campo.getLiderInimigo();
        if (lider.getTipo() != LiderEnum.STALIN) return;

        MonstroInstancia[] slotsInimigos = jogadorComStalin
                ? campo.getSlotsInimigo()
                : campo.getSlotsJogador();

        for (MonstroInstancia m : slotsInimigos) {
            if (m != null && m.getModoAtual() == ModoAcao.FARM) {
                m.adicionarPressao(1);
            }
        }
    }

    public void decrementarModosPresos(CampoBatalha campo) {
        decrementarSlots(campo.getSlotsJogador());
        decrementarSlots(campo.getSlotsInimigo());
    }

    private void decrementarSlots(MonstroInstancia[] slots) {
        for (MonstroInstancia m : slots) {
            if (m != null) m.decrementarPressoDef();
        }
    }

    public void forcarModosPresos(CampoBatalha campo) {
        forcarModoSlots(campo.getSlotsJogador());
        forcarModoSlots(campo.getSlotsInimigo());
    }

    private void forcarModoSlots(MonstroInstancia[] slots) {
        for (MonstroInstancia m : slots) {
            if (m != null && m.estaPressoDef()) {
                m.setModoAtual(ModoAcao.DEFESA);
            }
        }
    }

    public List<MonstroInstancia> getMonstrosVivos(MonstroInstancia[] slots) {
        List<MonstroInstancia> lista = new ArrayList<>();
        for (MonstroInstancia m : slots) {
            if (m != null) lista.add(m);
        }
        return lista;
    }
}