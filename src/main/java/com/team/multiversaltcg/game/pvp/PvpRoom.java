package com.team.multiversaltcg.game.pvp;

import com.team.multiversaltcg.game.model.TurnoJogador;
import com.team.multiversaltcg.game.service.GameService;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class PvpRoom {

    private String code;
    private PvpRoomStatus status;

    private String creatorId;
    private String creatorDeckId;
    private String creatorLiderId;

    private String guestId;
    private String guestDeckId;
    private String guestLiderId;

    private GameService gameService;
    private TurnoJogador creatorPendingTurn;
    private TurnoJogador guestPendingTurn;
    private boolean creatorSpecialPending;
    private boolean guestSpecialPending;

    @Builder.Default
    private List<String> lastLog = new ArrayList<>();

    public boolean hasPlayer(String username) {
        return username != null
                && (username.equalsIgnoreCase(creatorId)
                || (guestId != null && username.equalsIgnoreCase(guestId)));
    }

    public PvpSide sideOf(String username) {
        if (username != null && username.equalsIgnoreCase(creatorId)) return PvpSide.CREATOR;
        if (username != null && guestId != null && username.equalsIgnoreCase(guestId)) return PvpSide.GUEST;
        return null;
    }
}
