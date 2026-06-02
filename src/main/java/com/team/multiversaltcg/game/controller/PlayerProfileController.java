package com.team.multiversaltcg.game.controller;

import com.team.multiversaltcg.game.dto.PlayerProfileDTO;
import com.team.multiversaltcg.game.service.PlayerProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/players/{playerId}/profile")
public class PlayerProfileController {

    private final PlayerProfileService playerProfileService;

    public PlayerProfileController(PlayerProfileService playerProfileService) {
        this.playerProfileService = playerProfileService;
    }

    @GetMapping
    public PlayerProfileDTO getProfile(@PathVariable String playerId, Authentication authentication) {
        validarDono(playerId, authentication);
        return playerProfileService.ensureProfile(authentication.getName());
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
