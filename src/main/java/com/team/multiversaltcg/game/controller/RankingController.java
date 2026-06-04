package com.team.multiversaltcg.game.controller;

import com.team.multiversaltcg.game.dto.MatchHistoryDTO;
import com.team.multiversaltcg.game.dto.RankingEntryDTO;
import com.team.multiversaltcg.game.service.MatchHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RankingController {

    private final MatchHistoryService matchHistoryService;

    public RankingController(MatchHistoryService matchHistoryService) {
        this.matchHistoryService = matchHistoryService;
    }

    @GetMapping("/api/ranking")
    public List<RankingEntryDTO> ranking(@RequestParam(defaultValue = "50") int limit) {
        return matchHistoryService.ranking(limit);
    }

    @GetMapping("/api/players/{playerId}/matches")
    public List<MatchHistoryDTO> history(@PathVariable String playerId,
                                         @RequestParam(defaultValue = "30") int limit,
                                         Authentication authentication) {
        validarDono(playerId, authentication);
        return matchHistoryService.historyFor(authentication.getName(), limit);
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
