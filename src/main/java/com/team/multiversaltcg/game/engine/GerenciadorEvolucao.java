package com.team.multiversaltcg.game.engine;

import com.team.multiversaltcg.game.model.CampoBatalha;
import com.team.multiversaltcg.game.model.Carta;
import com.team.multiversaltcg.game.model.MonstroInstancia;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.service.CartaDataService;

import java.util.List;

public class GerenciadorEvolucao {

    private final CartaDataService cartaDataService;

    public GerenciadorEvolucao(CartaDataService cartaDataService) {
        this.cartaDataService = cartaDataService;
    }

    public void evoluir(CampoBatalha campo, Carta carta, int slot, boolean jogador, List<String> log) {
        MonstroInstancia[] slots = jogador ? campo.getSlotsJogador() : campo.getSlotsInimigo();
        MonstroInstancia base = slots[slot];
        if (base == null) throw new RegraInvalidaException("Nao ha monstro para evoluir nesse slot.");
        if (!base.getId().equals(carta.getBaseMonsterId())) {
            throw new RegraInvalidaException("Evolucao incompativel para " + base.getNome() + ".");
        }

        Carta evoluida = cartaDataService.getById(carta.getEvolvedMonsterId());
        if (evoluida == null || !evoluida.isMonstro()) {
            throw new RegraInvalidaException("Carta evoluida nao encontrada.");
        }

        MonstroInstancia novo = MonstroInstancia.fromCarta(evoluida);
        novo.setPressure(base.getPressure());
        novo.setStatusAtivos(base.getStatusAtivos());
        novo.setAtkBuff(base.getAtkBuff());
        novo.setDefBuff(base.getDefBuff());
        novo.setImune(base.isImune());
        novo.setImunoTurnos(base.getImunoTurnos());
        novo.setModoAtual(base.getModoAtual());
        novo.setTurnosPressoDef(base.getTurnosPressoDef());
        novo.setIgnorarDefBuffTurnos(base.getIgnorarDefBuffTurnos());
        novo.setCancelarProximoAtaque(base.isCancelarProximoAtaque());
        slots[slot] = novo;

        if ("angemon".equals(novo.getId())) {
            curarAliadoMaisPressionado(slots, 2);
        }

        log.add(base.getNome() + " evoluiu para " + novo.getNome() + ".");
    }

    private void curarAliadoMaisPressionado(MonstroInstancia[] slots, int quantidade) {
        MonstroInstancia alvo = null;
        for (MonstroInstancia m : slots) {
            if (m != null && (alvo == null || m.getPressure() > alvo.getPressure())) {
                alvo = m;
            }
        }
        if (alvo != null) alvo.curarPressao(quantidade);
    }
}
