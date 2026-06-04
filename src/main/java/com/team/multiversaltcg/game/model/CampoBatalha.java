package com.team.multiversaltcg.game.model;

import com.team.multiversaltcg.game.enums.FaseEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class CampoBatalha {

    private MonstroInstancia[] slotsJogador;
    private MonstroInstancia[] slotsInimigo;

    private List<Carta> deckJogador;
    private List<Carta> deckInimigo;

    private List<Carta> maoJogador;
    private List<Carta> maoInimigo;

    private List<Carta> descarteJogador;
    private List<Carta> descarteInimigo;

    private ZonaEfeito[] zonasEfeitoJogador;
    private ZonaEfeito[] zonasEfeitoInimigo;
    private Carta magiaAtivaJogador;
    private Carta magiaAtivaInimigo;
    private Carta armadilhaAtivaJogador;
    private Carta armadilhaAtivaInimigo;

    private Lider liderJogador;
    private Lider liderInimigo;

    private int auraJogador;
    private int auraInimigo;

    private int turnoAtual;
    private FaseEnum faseAtual;
    private boolean jogoEncerrado;
    private String vencedor;

    public static CampoBatalha criar(Lider liderJogador, Lider liderInimigo) {
        CampoBatalha c = new CampoBatalha();
        c.slotsJogador = new MonstroInstancia[3];
        c.slotsInimigo = new MonstroInstancia[3];
        c.deckJogador = new ArrayList<>();
        c.deckInimigo = new ArrayList<>();
        c.maoJogador = new ArrayList<>();
        c.maoInimigo = new ArrayList<>();
        c.descarteJogador = new ArrayList<>();
        c.descarteInimigo = new ArrayList<>();
        c.zonasEfeitoJogador = new ZonaEfeito[3];
        c.zonasEfeitoInimigo = new ZonaEfeito[3];
        c.magiaAtivaJogador = null;
        c.magiaAtivaInimigo = null;
        c.armadilhaAtivaJogador = null;
        c.armadilhaAtivaInimigo = null;
        c.liderJogador = liderJogador;
        c.liderInimigo = liderInimigo;
        c.auraJogador = 0;
        c.auraInimigo = 0;
        c.turnoAtual = 1;
        c.faseAtual = FaseEnum.ATRIBUICAO;
        c.jogoEncerrado = false;
        c.vencedor = null;
        return c;
    }

    public boolean temSlotVazio(boolean jogador) {
        MonstroInstancia[] slots = jogador ? slotsJogador : slotsInimigo;
        for (MonstroInstancia m : slots) {
            if (m == null) return true;
        }
        return false;
    }

    public int getSlotVazioIndex(boolean jogador) {
        MonstroInstancia[] slots = jogador ? slotsJogador : slotsInimigo;
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) return i;
        }
        return -1;
    }

    public int getMonstrosVivos(boolean jogador) {
        MonstroInstancia[] slots = jogador ? slotsJogador : slotsInimigo;
        int count = 0;
        for (MonstroInstancia m : slots) {
            if (m != null) count++;
        }
        return count;
    }

    public void adicionarAura(boolean jogador, int quantidade) {
        if (jogador) {
            auraJogador += quantidade;
        } else {
            auraInimigo += quantidade;
        }
    }

    public boolean gastarAura(boolean jogador, int quantidade) {
        if (jogador) {
            if (auraJogador < quantidade) return false;
            auraJogador -= quantidade;
        } else {
            if (auraInimigo < quantidade) return false;
            auraInimigo -= quantidade;
        }
        return true;
    }

    public void encerrarJogo(String vencedor) {
        this.jogoEncerrado = true;
        this.vencedor = vencedor;
        this.faseAtual = FaseEnum.FIM_JOGO;
    }

    public void embaralharDecks() {
        Collections.shuffle(deckJogador);
        Collections.shuffle(deckInimigo);
    }

    public Carta comprarCarta(boolean jogador) {
        List<Carta> deck = jogador ? deckJogador : deckInimigo;
        List<Carta> mao = jogador ? maoJogador : maoInimigo;
        if (deck.isEmpty()) return null;
        Carta carta = deck.remove(0);
        mao.add(carta);
        return carta;
    }

    public List<Carta> getMao(boolean jogador) {
        return jogador ? maoJogador : maoInimigo;
    }

    public List<Carta> getDeck(boolean jogador) {
        return jogador ? deckJogador : deckInimigo;
    }

    public List<Carta> getDescarte(boolean jogador) {
        return jogador ? descarteJogador : descarteInimigo;
    }

    public ZonaEfeito[] getZonasEfeito(boolean jogador) {
        return jogador ? zonasEfeitoJogador : zonasEfeitoInimigo;
    }

    public Carta getMagiaAtiva(boolean jogador) {
        return jogador ? magiaAtivaJogador : magiaAtivaInimigo;
    }

    public void setMagiaAtiva(boolean jogador, Carta carta) {
        if (jogador) {
            magiaAtivaJogador = carta;
        } else {
            magiaAtivaInimigo = carta;
        }
        sincronizarZonasEfeito(jogador);
    }

    public Carta getArmadilhaAtiva(boolean jogador) {
        return jogador ? armadilhaAtivaJogador : armadilhaAtivaInimigo;
    }

    public void setArmadilhaAtiva(boolean jogador, Carta carta) {
        if (jogador) {
            armadilhaAtivaJogador = carta;
        } else {
            armadilhaAtivaInimigo = carta;
        }
        sincronizarZonasEfeito(jogador);
    }

    public boolean temArmadilhaAtiva(boolean jogador) {
        return getArmadilhaAtiva(jogador) != null;
    }

    public boolean temMagiaAtiva(boolean jogador) {
        return getMagiaAtiva(jogador) != null;
    }

    public void sincronizarZonasEfeito(boolean jogador) {
        ZonaEfeito[] zonas = getZonasEfeito(jogador);
        zonas[0] = getMagiaAtiva(jogador) == null ? null : ZonaEfeito.aberta(getMagiaAtiva(jogador));
        zonas[1] = getArmadilhaAtiva(jogador) == null ? null : ZonaEfeito.armadilhaSetada(getArmadilhaAtiva(jogador));
        zonas[2] = null;
    }
}
