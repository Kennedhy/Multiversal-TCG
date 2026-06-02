package com.team.multiversaltcg.game.controller;

import com.team.multiversaltcg.game.dto.PvpRoomResponse;
import com.team.multiversaltcg.game.dto.PvpStateResponse;
import com.team.multiversaltcg.game.dto.TurnoDTO;
import com.team.multiversaltcg.game.model.PartidaEncerradaException;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.service.PvpService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/pvp")
public class PvpController {

    private final PvpService pvpService;

    public PvpController(PvpService pvpService) {
        this.pvpService = pvpService;
    }

    @PostMapping("/rooms")
    public PvpRoomResponse create(@RequestBody Map<String, String> body,
                                  Authentication authentication,
                                  HttpServletRequest request) {
        String codePlaceholder = "ROOM";
        return pvpService.createRoom(
                username(authentication),
                body.get("deckId"),
                body.getOrDefault("liderId", "MAO"),
                inviteUrl(request, codePlaceholder));
    }

    @PostMapping("/rooms/{code}/join")
    public PvpRoomResponse join(@PathVariable String code,
                                @RequestBody Map<String, String> body,
                                Authentication authentication,
                                HttpServletRequest request) {
        return pvpService.joinRoom(
                username(authentication),
                code,
                body.get("deckId"),
                body.getOrDefault("liderId", "MAO"),
                inviteUrl(request, code));
    }

    @GetMapping("/rooms/{code}")
    public PvpStateResponse state(@PathVariable String code, Authentication authentication) {
        return pvpService.getState(username(authentication), code);
    }

    @PostMapping("/rooms/{code}/turn")
    public PvpStateResponse turn(@PathVariable String code,
                                 @RequestBody(required = false) TurnoDTO turnoDTO,
                                 Authentication authentication) {
        if (turnoDTO == null) throw new RegraInvalidaException("Payload de turno e obrigatorio.");
        return pvpService.submitTurn(username(authentication), code, turnoDTO.toTurnoJogador());
    }

    @PostMapping("/rooms/{code}/special")
    public PvpStateResponse special(@PathVariable String code, Authentication authentication) {
        return pvpService.markSpecial(username(authentication), code);
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

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> acessoNegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("erro", ex.getMessage()));
    }

    private String username(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Token valido e obrigatorio.");
        }
        return authentication.getName();
    }

    private String inviteUrl(HttpServletRequest request, String code) {
        String origin = request.getHeader("Origin");
        String apiBase = request.getScheme() + "://" + request.getServerName()
                + (isDefaultPort(request) ? "" : ":" + request.getServerPort());
        String frontBase = origin == null || origin.isBlank() ? apiBase : origin;
        return frontBase + "/pvp.html?room=" + code + "&api="
                + URLEncoder.encode(apiBase, StandardCharsets.UTF_8);
    }

    private boolean isDefaultPort(HttpServletRequest request) {
        return ("http".equals(request.getScheme()) && request.getServerPort() == 80)
                || ("https".equals(request.getScheme()) && request.getServerPort() == 443);
    }
}
