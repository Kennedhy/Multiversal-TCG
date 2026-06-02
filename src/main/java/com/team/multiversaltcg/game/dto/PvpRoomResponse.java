package com.team.multiversaltcg.game.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PvpRoomResponse {

    private String code;
    private String status;
    private String side;
    private String inviteUrl;
}
