package com.team.multiversaltcg.game.controller;

import com.team.multiversaltcg.game.dto.EstadoJogoDTO;
import com.team.multiversaltcg.game.dto.TurnoDTO;
import com.team.multiversaltcg.game.model.CampoBatalha;
import com.team.multiversaltcg.game.model.PartidaEncerradaException;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.service.GameSessionManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameSessionManager sessionManager;

    public GameController(GameSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @PostMapping("/iniciar")
    public ResponseEntity<EstadoJogoDTO> iniciar(
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        String roomId = body.get("roomId");
        String liderId = body.get("liderId");
        String playerId = body.get("playerId");
        String deckId = body.get("deckId");
        if (roomId == null || roomId.isBlank()) {
            throw new RegraInvalidaException("roomId e obrigatorio.");
        }

        if (deckId != null && !deckId.isBlank()) {
            playerId = authenticatedUsername(authentication);
        } else {
            deckId = null;
            playerId = null;
        }

        CampoBatalha campo = sessionManager.iniciarSessao(roomId, liderId, playerId, deckId);

        return ResponseEntity.ok(
                EstadoJogoDTO.from(campo, List.of("Partida iniciada!"))
        );
    }

    @GetMapping("/estado/{roomId}")
    public ResponseEntity<EstadoJogoDTO> getEstado(
            @PathVariable String roomId) {

        if (!sessionManager.sessaoExiste(roomId)) {
            return ResponseEntity.notFound().build();
        }

        CampoBatalha campo = sessionManager.getEstado(roomId);
        List<String> log = sessionManager.getLog(roomId);

        return ResponseEntity.ok(EstadoJogoDTO.from(campo, log));
    }

    @PostMapping("/turno/{roomId}")
    public ResponseEntity<EstadoJogoDTO> processarTurno(
            @PathVariable String roomId,
            @RequestBody(required = false) TurnoDTO turnoDTO) {

        if (!sessionManager.sessaoExiste(roomId)) {
            return ResponseEntity.notFound().build();
        }

        if (turnoDTO == null) {
            throw new RegraInvalidaException("Payload de turno e obrigatorio.");
        }

        List<String> log = sessionManager.processarTurno(roomId, turnoDTO.toTurnoJogador());
        CampoBatalha campo = sessionManager.sessaoExiste(roomId)
                ? sessionManager.getEstado(roomId)
                : null;

        if (campo == null) {
            return ResponseEntity.ok(
                    EstadoJogoDTO.builder()
                            .jogoEncerrado(true)
                            .log(log)
                            .build()
            );
        }

        return ResponseEntity.ok(EstadoJogoDTO.from(campo, log));
    }

    @PostMapping("/especial/{roomId}")
    public ResponseEntity<EstadoJogoDTO> ativarEspecial(
            @PathVariable String roomId) {

        if (!sessionManager.sessaoExiste(roomId)) {
            return ResponseEntity.notFound().build();
        }

        sessionManager.ativarEspecial(roomId);
        CampoBatalha campo = sessionManager.getEstado(roomId);
        List<String> log = sessionManager.getLog(roomId);

        return ResponseEntity.ok(EstadoJogoDTO.from(campo, log));
    }

    @ExceptionHandler(RegraInvalidaException.class)
    public ResponseEntity<Map<String, String>> regraInvalida(RegraInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(PartidaEncerradaException.class)
    public ResponseEntity<Map<String, String>> partidaEncerrada(PartidaEncerradaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> acessoNegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erro", ex.getMessage()));
    }

    private String authenticatedUsername(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Token valido e obrigatorio para iniciar com deck salvo.");
        }
        return authentication.getName();
    }
}
