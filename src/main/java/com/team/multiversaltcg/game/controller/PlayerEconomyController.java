package com.team.multiversaltcg.game.controller;

import com.team.multiversaltcg.game.dto.CollectionDTO;
import com.team.multiversaltcg.game.dto.PackOpeningDTO;
import com.team.multiversaltcg.game.dto.ShopDTO;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.service.PackService;
import com.team.multiversaltcg.game.service.PlayerCollectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/players/{playerId}")
public class PlayerEconomyController {

    private final PlayerCollectionService collectionService;
    private final PackService packService;

    public PlayerEconomyController(PlayerCollectionService collectionService, PackService packService) {
        this.collectionService = collectionService;
        this.packService = packService;
    }

    @GetMapping("/collection")
    public CollectionDTO collection(@PathVariable String playerId, Authentication authentication) {
        String owner = authenticatedOwner(playerId, authentication);
        return collectionService.getCollection(owner);
    }

    @GetMapping("/shop")
    public ShopDTO shop(@PathVariable String playerId, Authentication authentication) {
        String owner = authenticatedOwner(playerId, authentication);
        return packService.getShop(owner);
    }

    @PostMapping("/packs/buy")
    public PackOpeningDTO buyPack(@PathVariable String playerId, Authentication authentication) {
        String owner = authenticatedOwner(playerId, authentication);
        return packService.buyPack(owner);
    }

    @GetMapping("/packs/history")
    public List<PackOpeningDTO> history(@PathVariable String playerId, Authentication authentication) {
        String owner = authenticatedOwner(playerId, authentication);
        return packService.history(owner);
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

    private String authenticatedOwner(String playerId, Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Token valido e obrigatorio.");
        }
        if (!playerId.trim().equalsIgnoreCase(authentication.getName())) {
            throw new AccessDeniedException("Jogador autenticado nao pode acessar recursos de outro playerId.");
        }
        return authentication.getName();
    }
}
