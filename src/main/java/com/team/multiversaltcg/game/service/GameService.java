package com.team.multiversaltcg.game.service;

import com.team.multiversaltcg.game.engine.MotorDeJogo;
import com.team.multiversaltcg.game.engine.GerenciadorCompra;
import com.team.multiversaltcg.game.enums.LiderEnum;
import com.team.multiversaltcg.game.model.CampoBatalha;
import com.team.multiversaltcg.game.model.Carta;
import com.team.multiversaltcg.game.model.Lider;
import com.team.multiversaltcg.game.model.PartidaEncerradaException;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.model.TurnoJogador;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class GameService {

    private final CartaDataService cartaDataService;
    private final GerenciadorCompra gerenciadorCompra;
    private MotorDeJogo motor;
    private CampoBatalha campo;

    public GameService(CartaDataService cartaDataService) {
        this.cartaDataService = cartaDataService;
        this.gerenciadorCompra = new GerenciadorCompra();
    }

    public CampoBatalha iniciarPartida(String liderIdJogador) {
        return iniciarPartida(liderIdJogador, null);
    }

    public CampoBatalha iniciarPartida(String liderIdJogador, List<Carta> deckJogadorCustom) {
        LiderEnum tipoLider = parseLider(liderIdJogador);

        Lider liderJogador = Lider.criar(tipoLider);
        Lider liderInimigo = Lider.criar(LiderEnum.GENGHIS);

        campo = CampoBatalha.criar(liderJogador, liderInimigo);
        campo.getDeckJogador().addAll(deckJogadorCustom == null ? cartaDataService.getDeckPadrao() : deckJogadorCustom);
        campo.getDeckInimigo().addAll(cartaDataService.getDeckPadrao());
        campo.embaralharDecks();

        gerenciadorCompra.comprarMaoInicial(campo, true);
        gerenciadorCompra.comprarMaoInicial(campo, false);

        motor = new MotorDeJogo(cartaDataService);
        motor.iniciarPartida(campo);

        return campo;
    }

    public CampoBatalha iniciarPartidaPvp(String liderIdJogador,
                                          String liderIdInimigo,
                                          List<Carta> deckJogadorCustom,
                                          List<Carta> deckInimigoCustom) {
        LiderEnum tipoLiderJogador = parseLider(liderIdJogador);
        LiderEnum tipoLiderInimigo = parseLider(liderIdInimigo);

        Lider liderJogador = Lider.criar(tipoLiderJogador);
        Lider liderInimigo = Lider.criar(tipoLiderInimigo);

        campo = CampoBatalha.criar(liderJogador, liderInimigo);
        campo.getDeckJogador().addAll(deckJogadorCustom);
        campo.getDeckInimigo().addAll(deckInimigoCustom);
        campo.embaralharDecks();

        gerenciadorCompra.comprarMaoInicial(campo, true);
        gerenciadorCompra.comprarMaoInicial(campo, false);

        motor = new MotorDeJogo(cartaDataService);
        motor.iniciarPartida(campo);

        return campo;
    }

    private LiderEnum parseLider(String liderIdJogador) {
        if (liderIdJogador == null || liderIdJogador.isBlank()) {
            throw new RegraInvalidaException("Lider e obrigatorio.");
        }
        try {
            return LiderEnum.valueOf(liderIdJogador.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new RegraInvalidaException("Lider invalido: " + liderIdJogador);
        }
    }

    public List<String> processarTurno(TurnoJogador turno) {
        if (campo == null) {
            throw new RegraInvalidaException("Partida nao iniciada.");
        }
        if (campo.isJogoEncerrado()) {
            throw new PartidaEncerradaException("Partida ja encerrada.");
        }
        return motor.processarTurno(turno);
    }

    public List<String> processarTurnoPvp(TurnoJogador turnoJogador,
                                          TurnoJogador turnoInimigo,
                                          boolean jogadorPrimeiro,
                                          boolean especialJogador,
                                          boolean especialInimigo) {
        if (campo == null) {
            throw new RegraInvalidaException("Partida nao iniciada.");
        }
        if (campo.isJogoEncerrado()) {
            throw new PartidaEncerradaException("Partida ja encerrada.");
        }
        return motor.processarTurnoPvp(turnoJogador, turnoInimigo, jogadorPrimeiro, especialJogador, especialInimigo);
    }

    public void ativarEspecial() {
        if (campo == null) {
            throw new RegraInvalidaException("Partida nao iniciada.");
        }
        if (campo.isJogoEncerrado()) {
            throw new PartidaEncerradaException("Partida ja encerrada.");
        }
        motor.ativarEspecial(true);
    }

    public CampoBatalha getCampo() {
        return campo;
    }

    public List<String> getLogTurno() {
        return motor != null ? motor.getLogTurno() : List.of();
    }

    public Carta getCarta(String id) {
        return cartaDataService.getById(id);
    }
}
