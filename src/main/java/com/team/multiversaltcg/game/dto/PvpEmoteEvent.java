package com.team.multiversaltcg.game.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class PvpEmoteEvent {

    private String roomCode;
    private String senderId;
    private String senderSide;
    private String emoteId;
    private String gifUrl;
    private Instant sentAt;
}
