package com.team.multiversaltcg.game.engine;

import com.team.multiversaltcg.game.model.CampoBatalha;
import com.team.multiversaltcg.game.model.Carta;

import java.util.List;

public class GerenciadorCompra {

    public static final int MAO_INICIAL = 4;

    public void comprarMaoInicial(CampoBatalha campo, boolean jogador) {
        for (int i = 0; i < MAO_INICIAL; i++) {
            comprar(campo, jogador, null, false);
        }
        garantirMonstroInicial(campo, jogador);
    }

    public Carta comprarTurno(CampoBatalha campo, boolean jogador, List<String> log) {
        return comprar(campo, jogador, log, false);
    }

    public Carta comprar(CampoBatalha campo, boolean jogador, List<String> log, boolean derrotaSeVazio) {
        List<Carta> deck = campo.getDeck(jogador);
        if (deck.isEmpty()) {
            if (derrotaSeVazio) {
                campo.encerrarJogo(jogador ? "INIMIGO" : "JOGADOR");
                if (log != null) {
                    log.add((jogador ? "Jogador" : "Inimigo") + " tentou comprar com deck vazio e perdeu.");
                }
            }
            return null;
        }

        Carta carta = deck.remove(0);
        campo.getMao(jogador).add(carta);
        if (log != null) {
            log.add((jogador ? "Jogador" : "Inimigo") + " comprou 1 carta"
                    + (jogador ? ": " + carta.getNome() : "") + ".");
        }
        return carta;
    }

    private void garantirMonstroInicial(CampoBatalha campo, boolean jogador) {
        List<Carta> mao = campo.getMao(jogador);
        if (mao.stream().anyMatch(Carta::isMonstro)) return;

        List<Carta> deck = campo.getDeck(jogador);
        for (int i = 0; i < deck.size(); i++) {
            Carta carta = deck.get(i);
            if (carta.isMonstro()) {
                Carta substituida = mao.remove(0);
                mao.add(carta);
                deck.set(i, substituida);
                return;
            }
        }
    }
}
