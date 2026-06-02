package com.team.multiversaltcg.game.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PvpRoomViewDTO {

    private String code;
    private String status;
    private String creatorId;
    private String guestId;
    private String side;
    private int turnoAtual;
}
