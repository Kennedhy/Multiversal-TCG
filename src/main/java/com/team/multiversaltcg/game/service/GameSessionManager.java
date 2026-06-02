package com.team.multiversaltcg.game.service;

import com.team.multiversaltcg.game.model.CampoBatalha;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.model.TurnoJogador;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameSessionManager {

    private final Map<String, GameService> sessoes = new ConcurrentHashMap<>();
    private final CartaDataService cartaDataService;
    private final PlayerDeckService playerDeckService;

    public GameSessionManager(CartaDataService cartaDataService, PlayerDeckService playerDeckService) {
        this.cartaDataService = cartaDataService;
        this.playerDeckService = playerDeckService;
    }

    public CampoBatalha iniciarSessao(String roomId, String liderId) {
        return iniciarSessao(roomId, liderId, null, null);
    }

    public CampoBatalha iniciarSessao(String roomId, String liderId, String playerId, String deckId) {
        GameService gameService = new GameService(cartaDataService);
        CampoBatalha campo = deckId == null || deckId.isBlank()
                ? gameService.iniciarPartida(liderId)
                : gameService.iniciarPartida(liderId, playerDeckService.montarDeck(playerId, deckId));
        sessoes.put(roomId, gameService);
        return campo;
    }

    public List<String> processarTurno(String roomId, TurnoJogador turno) {
        GameService service = getSessao(roomId);
        List<String> log = service.processarTurno(turno);
        if (service.getCampo().isJogoEncerrado()) {
            encerrarSessao(roomId);
        }
        return log;
    }

    public void ativarEspecial(String roomId) {
        getSessao(roomId).ativarEspecial();
    }

    public CampoBatalha getEstado(String roomId) {
        return getSessao(roomId).getCampo();
    }

    public List<String> getLog(String roomId) {
        return getSessao(roomId).getLogTurno();
    }

    public boolean sessaoExiste(String roomId) {
        return sessoes.containsKey(roomId);
    }

    public void encerrarSessao(String roomId) {
        sessoes.remove(roomId);
    }

    private GameService getSessao(String roomId) {
        GameService service = sessoes.get(roomId);
        if (service == null) {
            throw new RegraInvalidaException("Sessao nao encontrada: " + roomId);
        }
        return service;
    }
}
