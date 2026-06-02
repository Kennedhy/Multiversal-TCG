package com.team.multiversaltcg.game.engine;

import com.team.multiversaltcg.game.model.CampoBatalha;
import com.team.multiversaltcg.game.model.Carta;
import com.team.multiversaltcg.game.model.Lider;
import com.team.multiversaltcg.game.model.MonstroInstancia;

import java.util.ArrayList;
import java.util.List;

public class GerenciadorKO {

    private static final int DANO_HP_POR_KO = 10;

    public List<String> verificarEProcessar(CampoBatalha campo) {
        List<String> logs = new ArrayList<>();

        logs.addAll(processarSlots(campo, true));
        logs.addAll(processarSlots(campo, false));

        verificarFimDeJogo(campo);

        return logs;
    }

    private List<String> processarSlots(CampoBatalha campo, boolean jogador) {
        List<String> logs = new ArrayList<>();
        MonstroInstancia[] slots = jogador
                ? campo.getSlotsJogador()
                : campo.getSlotsInimigo();
        List<Carta> descarte = jogador
                ? campo.getDescarteJogador()
                : campo.getDescarteInimigo();
        Lider lider = jogador
                ? campo.getLiderJogador()
                : campo.getLiderInimigo();

        for (int i = 0; i < slots.length; i++) {
            MonstroInstancia m = slots[i];
            if (m != null && m.isNocauteado()) {
                String dono = jogador ? "Seu" : "Inimigo";
                logs.add(dono + " " + m.getNome() + " foi nocauteado.");

                descarte.add(m.getTemplate());
                slots[i] = null;

                lider.perderHp(DANO_HP_POR_KO);
                logs.add(dono + " Lider perde 10 HP. HP restante: " + lider.getHp());
            }
        }

        return logs;
    }

    private void verificarFimDeJogo(CampoBatalha campo) {
        if (campo.getLiderJogador().isDerrotado()) {
            campo.encerrarJogo("INIMIGO");
            return;
        }
        if (campo.getLiderInimigo().isDerrotado()) {
            campo.encerrarJogo("JOGADOR");
        }
    }

    public boolean podeInvocar(CampoBatalha campo, boolean jogador) {
        return campo.temSlotVazio(jogador) &&
                !(jogador
                        ? campo.getDeckJogador().isEmpty() &&
                        campo.getMaoJogador().isEmpty()
                        : campo.getDeckInimigo().isEmpty() &&
                        campo.getMaoInimigo().isEmpty());
    }

    public void invocarDoDescarte(CampoBatalha campo, boolean jogador, int slotDestino) {
        List<Carta> descarte = jogador
                ? campo.getDescarteJogador()
                : campo.getDescarteInimigo();

        if (descarte.isEmpty()) return;

        MonstroInstancia[] slots = jogador
                ? campo.getSlotsJogador()
                : campo.getSlotsInimigo();

        if (slots[slotDestino] != null) return;

        MonstroInstancia ultimo = MonstroInstancia.fromCarta(
                descarte.get(descarte.size() - 1));
        ultimo.adicionarPressao(1);
        slots[slotDestino] = ultimo;
    }
}
