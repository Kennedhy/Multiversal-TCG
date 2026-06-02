package com.team.multiversaltcg.game.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PvpStateResponse {

    private PvpRoomViewDTO room;
    private EstadoJogoDTO state;
    private boolean pendingSelf;
    private boolean pendingOpponent;
}
