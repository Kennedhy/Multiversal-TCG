package com.team.multiversaltcg.game.model;

import com.team.multiversaltcg.game.collections.Pilha;
import com.team.multiversaltcg.game.enums.ModoAcao;
import com.team.multiversaltcg.game.enums.StatusEnum;
import com.team.multiversaltcg.game.enums.TipoUniversal;
import lombok.Data;

@Data
public class MonstroInstancia {

    private String id;
    private String nome;
    private String imageUrl;
    private TipoUniversal tipo;
    private String universo;

    private int atkBase;
    private int defBase;
    private int atkBuff;
    private int defBuff;

    private int pressure;
    private boolean imune;
    private int imunoTurnos;

    private ModoAcao modoAtual;
    private int turnosPressoDef;
    private int ignorarDefBuffTurnos;
    private boolean cancelarProximoAtaque;

    private Pilha<StatusAtivo> statusAtivos;
    private Carta template;

    public static MonstroInstancia fromCarta(Carta carta) {
        if (carta == null || !carta.isMonstro()) {
            throw new IllegalArgumentException("Apenas cartas de monstro podem entrar no campo.");
        }
        MonstroInstancia m = new MonstroInstancia();
        m.id = carta.getId();
        m.nome = carta.getNome();
        m.imageUrl = carta.getImageUrlOrPlaceholder();
        m.tipo = carta.getTipo();
        m.universo = carta.getUniverso();
        m.atkBase = carta.getAtk();
        m.defBase = carta.getDef();
        m.atkBuff = 0;
        m.defBuff = 0;
        m.pressure = 0;
        m.imune = false;
        m.imunoTurnos = 0;
        m.modoAtual = ModoAcao.ATAQUE;
        m.turnosPressoDef = 0;
        m.ignorarDefBuffTurnos = 0;
        m.cancelarProximoAtaque = false;
        m.statusAtivos = new Pilha<>();
        m.template = carta;
        return m;
    }

    public int getAtkTotal() {
        return atkBase + atkBuff;
    }

    public int getDefTotal(boolean modoDefesa, int bonusDefLider) {
        int def = defBase + (ignorarDefBuffTurnos > 0 ? 0 : defBuff) + bonusDefLider;
        if (modoDefesa && ignorarDefBuffTurnos <= 0) {
            def += 25;
            if ("blastoise".equals(id)) def += 40;
        }
        return def;
    }

    public boolean isNocauteado() {
        return pressure >= 3;
    }

    public boolean estaPressoDef() {
        return turnosPressoDef > 0;
    }

    public void decrementarPressoDef() {
        if (turnosPressoDef > 0) turnosPressoDef--;
        if (ignorarDefBuffTurnos > 0) ignorarDefBuffTurnos--;
    }

    public void adicionarPressao(int quantidade) {
        pressure = Math.min(3, pressure + quantidade);
    }

    public void curarPressao(int quantidade) {
        pressure = Math.max(0, pressure - quantidade);
    }

    public void aplicarStatus(StatusEnum tipo, int duracao) {
        if (imune) return;
        if (temStatus(tipo)) return;
        StatusAtivo novo = StatusAtivo.builder()
                .tipo(tipo)
                .turnosRestantes(duracao)
                .tickContador(0)
                .build();
        statusAtivos.push(novo);
    }

    public void limparStatus() {
        statusAtivos.clear();
    }

    public boolean temStatus(StatusEnum tipo) {
        if (statusAtivos == null || statusAtivos.estaVazia()) return false;
        return statusAtivos.toList().stream().anyMatch(s -> s.getTipo() == tipo);
    }

    public void decrementarImunidade() {
        if (imunoTurnos > 0) {
            imunoTurnos--;
            if (imunoTurnos == 0) imune = false;
        }
    }
}
