package com.team.multiversaltcg.game.enums;

import java.util.Locale;

public enum PvpEmote {

    HELLO("Ola", "/assets/emotes/hello.gif"),
    GOOD_GAME("Bom jogo", "/assets/emotes/good_game.gif"),
    THANKS("Obrigado", "/assets/emotes/thanks.gif"),
    WOW("Uau", "/assets/emotes/wow.gif"),
    OOPS("Foi mal", "/assets/emotes/oops.gif"),
    ANGRY("Bravo", "/assets/emotes/angry.gif");

    private final String displayName;
    private final String gifUrl;

    PvpEmote(String displayName, String gifUrl) {
        this.displayName = displayName;
        this.gifUrl = gifUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getGifUrl() {
        return gifUrl;
    }

    public static PvpEmote fromId(String id) {
        if (id == null || id.isBlank()) return null;
        try {
            return PvpEmote.valueOf(id.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
