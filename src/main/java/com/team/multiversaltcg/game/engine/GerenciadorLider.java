package com.team.multiversaltcg.game.engine;

import com.team.multiversaltcg.game.enums.LiderEnum;
import com.team.multiversaltcg.game.enums.ModoAcao;
import com.team.multiversaltcg.game.model.CampoBatalha;
import com.team.multiversaltcg.game.model.Lider;
import com.team.multiversaltcg.game.model.MonstroInstancia;

import java.util.ArrayList;
import java.util.List;

public class GerenciadorLider {

    public boolean verificarBonusOfensivoRei(CampoBatalha campo, boolean jogador) {
        MonstroInstancia[] slots = jogador
                ? campo.getSlotsJogador()
                : campo.getSlotsInimigo();
        for (MonstroInstancia m : slots) {
            if (m != null && m.getModoAtual() != ModoAcao.ATAQUE) return false;
        }
        return true;
    }

    public void aplicarPassivaPressaoFarm(CampoBatalha campo, boolean jogadorComZagueiro) {
        Lider lider = jogadorComZagueiro
                ? campo.getLiderJogador()
                : campo.getLiderInimigo();
        if (lider.getTipo() != LiderEnum.STALIN) return;

        MonstroInstancia[] slotsInimigos = jogadorComZagueiro
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
