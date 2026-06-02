package com.team.multiversaltcg.game.engine;

import com.team.multiversaltcg.game.collections.Pilha;
import com.team.multiversaltcg.game.enums.StatusEnum;
import com.team.multiversaltcg.game.model.MonstroInstancia;
import com.team.multiversaltcg.game.model.StatusAtivo;

import java.util.List;

public class GerenciadorStatus {

    public void resolverTick(MonstroInstancia monstro) {
        if (monstro == null || monstro.getStatusAtivos().estaVazia()) return;
        if (monstro.isImune()) {
            monstro.decrementarImunidade();
            return;
        }

        List<StatusAtivo> ativos = monstro.getStatusAtivos().toList();
        Pilha<StatusAtivo> novaPilha = new Pilha<>();

        for (int i = ativos.size() - 1; i >= 0; i--) {
            StatusAtivo status = ativos.get(i);
            status.incrementarTick();

            if (status.deveAplicarDano()) {
                monstro.adicionarPressao(1);
            }

            status.decrementar();

            if (!status.expirou()) {
                novaPilha.push(status);
            }
        }

        monstro.setStatusAtivos(novaPilha);
    }

    public void aplicarStatus(MonstroInstancia alvo, StatusEnum tipo, int duracao) {
        if (alvo == null || alvo.isImune()) return;
        if (temStatus(alvo, tipo)) return;

        StatusAtivo novo = StatusAtivo.builder()
                .tipo(tipo)
                .turnosRestantes(duracao)
                .tickContador(0)
                .build();
        alvo.getStatusAtivos().push(novo);
    }

    public void limparTodos(MonstroInstancia monstro) {
        if (monstro == null) return;
        monstro.limparStatus();
    }

    public boolean temStatus(MonstroInstancia monstro, StatusEnum tipo) {
        if (monstro == null || monstro.getStatusAtivos().estaVazia()) return false;
        List<StatusAtivo> ativos = monstro.getStatusAtivos().toList();
        for (StatusAtivo s : ativos) {
            if (s.getTipo() == tipo) return true;
        }
        return false;
    }

    public void resolverTodosMonstros(MonstroInstancia[] slots) {
        for (MonstroInstancia m : slots) {
            if (m != null) resolverTick(m);
        }
    }
}