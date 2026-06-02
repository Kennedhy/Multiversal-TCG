package com.team.multiversaltcg.game.controller;

import com.team.multiversaltcg.game.dto.PlayerDeckDTO;
import com.team.multiversaltcg.game.dto.PlayerDeckSummaryDTO;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.service.PlayerDeckService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/players/{playerId}/decks")
public class PlayerDeckController {

    private final PlayerDeckService playerDeckService;

    public PlayerDeckController(PlayerDeckService playerDeckService) {
        this.playerDeckService = playerDeckService;
    }

    @GetMapping
    public List<PlayerDeckSummaryDTO> listar(@PathVariable String playerId, Authentication authentication) {
        validarDono(playerId, authentication);
        return playerDeckService.listar(playerId);
    }

    @GetMapping("/{deckId}")
    public PlayerDeckDTO buscar(@PathVariable String playerId, @PathVariable String deckId, Authentication authentication) {
        validarDono(playerId, authentication);
        return playerDeckService.buscar(playerId, deckId);
    }

    @PostMapping
    public PlayerDeckDTO criar(@PathVariable String playerId, @RequestBody PlayerDeckDTO dto, Authentication authentication) {
        validarDono(playerId, authentication);
        return playerDeckService.criar(playerId, dto);
    }

    @PostMapping("/copy-default")
    public PlayerDeckDTO copiarPadrao(@PathVariable String playerId,
                                      @RequestBody(required = false) Map<String, String> body,
                                      Authentication authentication) {
        validarDono(playerId, authentication);
        String name = body == null ? null : body.get("name");
        return playerDeckService.copiarPadrao(playerId, name);
    }

    @PutMapping("/{deckId}")
    public PlayerDeckDTO atualizar(@PathVariable String playerId,
                                   @PathVariable String deckId,
                                   @RequestBody PlayerDeckDTO dto,
                                   Authentication authentication) {
        validarDono(playerId, authentication);
        return playerDeckService.atualizar(playerId, deckId, dto);
    }

    @DeleteMapping("/{deckId}")
    public ResponseEntity<Void> excluir(@PathVariable String playerId,
                                        @PathVariable String deckId,
                                        Authentication authentication) {
        validarDono(playerId, authentication);
        playerDeckService.excluir(playerId, deckId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(RegraInvalidaException.class)
    public ResponseEntity<Map<String, String>> regraInvalida(RegraInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> acessoNegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erro", ex.getMessage()));
    }

    private void validarDono(String playerId, Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Token valido e obrigatorio.");
        }
        if (!playerId.trim().equalsIgnoreCase(authentication.getName())) {
            throw new AccessDeniedException("Jogador autenticado nao pode acessar recursos de outro playerId.");
        }
    }
}
