package com.team.multiversaltcg.game.service;

import com.team.multiversaltcg.game.dto.EstadoJogoDTO;
import com.team.multiversaltcg.game.dto.PvpRoomResponse;
import com.team.multiversaltcg.game.dto.PvpRoomViewDTO;
import com.team.multiversaltcg.game.dto.PvpStateResponse;
import com.team.multiversaltcg.game.enums.LiderEnum;
import com.team.multiversaltcg.game.model.Carta;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.model.TurnoJogador;
import com.team.multiversaltcg.game.pvp.PvpRoom;
import com.team.multiversaltcg.game.pvp.PvpRoomStatus;
import com.team.multiversaltcg.game.pvp.PvpSide;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PvpService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final Map<String, PvpRoom> rooms = new ConcurrentHashMap<>();
    private final PlayerDeckService playerDeckService;
    private final CartaDataService cartaDataService;
    private final SimpMessagingTemplate messagingTemplate;
    private final SecureRandom random = new SecureRandom();

    public PvpService(PlayerDeckService playerDeckService,
                      CartaDataService cartaDataService,
                      SimpMessagingTemplate messagingTemplate) {
        this.playerDeckService = playerDeckService;
        this.cartaDataService = cartaDataService;
        this.messagingTemplate = messagingTemplate;
    }

    public PvpRoomResponse createRoom(String username, String deckId, String liderId, String inviteUrl) {
        validateDeckIfPresent(username, deckId);
        String code = newCode();
        PvpRoom room = PvpRoom.builder()
                .code(code)
                .status(PvpRoomStatus.WAITING)
                .creatorId(normalize(username))
                .creatorDeckId(deckId)
                .creatorLiderId(normalizeLeader(liderId))
                .lastLog(List.of("Sala PvP criada. Aguardando oponente."))
                .build();
        rooms.put(code, room);
        publish(room);
        return roomResponse(room, PvpSide.CREATOR, inviteUrl);
    }

    public PvpRoomResponse joinRoom(String username, String code, String deckId, String liderId, String inviteUrl) {
        PvpRoom room = getRoom(code);
        synchronized (room) {
            if (room.getStatus() != PvpRoomStatus.WAITING) {
                throw new RegraInvalidaException("Sala nao esta aguardando oponente.");
            }
            String player = normalize(username);
            if (player.equals(room.getCreatorId())) {
                throw new RegraInvalidaException("O criador nao pode entrar como oponente.");
            }
            validateDeckIfPresent(player, deckId);
            room.setGuestId(player);
            room.setGuestDeckId(deckId);
            room.setGuestLiderId(normalizeLeader(liderId));
            room.setStatus(PvpRoomStatus.READY);

            GameService gameService = new GameService(cartaDataService);
            gameService.iniciarPartidaPvp(
                    room.getCreatorLiderId(),
                    room.getGuestLiderId(),
                    deckFor(room.getCreatorId(), room.getCreatorDeckId()),
                    deckFor(room.getGuestId(), room.getGuestDeckId()));
            room.setGameService(gameService);
            room.setLastLog(List.of("Oponente entrou. Partida PvP iniciada."));
            room.setStatus(PvpRoomStatus.IN_PROGRESS);
        }
        publish(room);
        return roomResponse(room, PvpSide.GUEST, inviteUrl);
    }

    public PvpStateResponse getState(String username, String code) {
        PvpRoom room = getRoomForPlayer(username, code);
        return stateFor(room, normalize(username));
    }

    public PvpStateResponse submitTurn(String username, String code, TurnoJogador turn) {
        PvpRoom room = getRoomForPlayer(username, code);
        synchronized (room) {
            if (room.getStatus() != PvpRoomStatus.IN_PROGRESS) {
                throw new RegraInvalidaException("Partida PvP ainda nao esta em andamento.");
            }
            PvpSide side = room.sideOf(normalize(username));
            if (side == PvpSide.CREATOR) {
                room.setCreatorPendingTurn(turn);
            } else {
                room.setGuestPendingTurn(turn);
            }

            if (room.getCreatorPendingTurn() != null && room.getGuestPendingTurn() != null) {
                boolean creatorFirst = room.getGameService().getCampo().getTurnoAtual() % 2 == 1;
                List<String> log = room.getGameService().processarTurnoPvp(
                        room.getCreatorPendingTurn(),
                        room.getGuestPendingTurn(),
                        creatorFirst);
                room.setLastLog(log);
                room.setCreatorPendingTurn(null);
                room.setGuestPendingTurn(null);
                if (room.getGameService().getCampo().isJogoEncerrado()) {
                    room.setStatus(PvpRoomStatus.FINISHED);
                }
            } else {
                room.setLastLog(List.of("Aguardando o outro jogador finalizar o turno."));
            }
        }
        publish(room);
        return stateFor(room, normalize(username));
    }

    private void validateDeckIfPresent(String username, String deckId) {
        if (deckId != null && !deckId.isBlank()) {
            playerDeckService.montarDeck(normalize(username), deckId);
        }
    }

    private List<Carta> deckFor(String username, String deckId) {
        if (deckId == null || deckId.isBlank()) {
            return cartaDataService.getDeckPadrao();
        }
        return playerDeckService.montarDeck(normalize(username), deckId);
    }

    private PvpRoom getRoomForPlayer(String username, String code) {
        PvpRoom room = getRoom(code);
        if (!room.hasPlayer(normalize(username))) {
            throw new AccessDeniedException("Jogador nao pertence a sala PvP.");
        }
        return room;
    }

    private PvpRoom getRoom(String code) {
        PvpRoom room = rooms.get(normalizeCode(code));
        if (room == null) {
            throw new RegraInvalidaException("Sala PvP nao encontrada.");
        }
        return room;
    }

    private PvpStateResponse stateFor(PvpRoom room, String username) {
        PvpSide side = room.sideOf(username);
        boolean creator = side == PvpSide.CREATOR;
        boolean hasGame = room.getGameService() != null && room.getGameService().getCampo() != null;

        return PvpStateResponse.builder()
                .room(roomView(room, side))
                .state(hasGame
                        ? EstadoJogoDTO.fromPerspective(room.getGameService().getCampo(), room.getLastLog(), creator)
                        : null)
                .pendingSelf(creator ? room.getCreatorPendingTurn() != null : room.getGuestPendingTurn() != null)
                .pendingOpponent(creator ? room.getGuestPendingTurn() != null : room.getCreatorPendingTurn() != null)
                .build();
    }

    private PvpRoomViewDTO roomView(PvpRoom room, PvpSide side) {
        int turno = room.getGameService() == null || room.getGameService().getCampo() == null
                ? 0
                : room.getGameService().getCampo().getTurnoAtual();
        return PvpRoomViewDTO.builder()
                .code(room.getCode())
                .status(room.getStatus().name())
                .creatorId(room.getCreatorId())
                .guestId(room.getGuestId())
                .side(side == null ? null : side.name())
                .turnoAtual(turno)
                .build();
    }

    private PvpRoomResponse roomResponse(PvpRoom room, PvpSide side, String inviteUrl) {
        String finalInviteUrl = inviteUrl == null ? null : inviteUrl.replace("ROOM", room.getCode());
        return PvpRoomResponse.builder()
                .code(room.getCode())
                .status(room.getStatus().name())
                .side(side.name())
                .inviteUrl(finalInviteUrl)
                .build();
    }

    private void publish(PvpRoom room) {
        sendTo(room, room.getCreatorId());
        if (room.getGuestId() != null) sendTo(room, room.getGuestId());
    }

    private void sendTo(PvpRoom room, String username) {
        if (username == null || username.isBlank()) return;
        messagingTemplate.convertAndSendToUser(username, "/queue/pvp/" + room.getCode(), stateFor(room, username));
    }

    private String newCode() {
        String code;
        do {
            StringBuilder builder = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                builder.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
            }
            code = builder.toString();
        } while (rooms.containsKey(code));
        return code;
    }

    private String normalize(String username) {
        if (username == null || username.isBlank()) {
            throw new AccessDeniedException("Token valido e obrigatorio.");
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) throw new RegraInvalidaException("Codigo da sala e obrigatorio.");
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeLeader(String liderId) {
        LiderEnum lider = LiderEnum.fromId(
                liderId == null || liderId.isBlank() ? LiderEnum.defaultId() : liderId);
        if (lider == null) {
            throw new RegraInvalidaException("Lider invalido: " + liderId);
        }
        return lider.name();
    }
}
