package com.team.multiversaltcg.game.enums;

import java.util.Locale;
import java.util.Map;

public enum LiderEnum {

    ASH(
            "Ash Ketchum",
            "Monstros em Modo Farm geram +2 Aura extra."
    ),
    TAI(
            "Tai Kamiya",
            "Bloqueios em Modo Defesa causam 8 HP direto ao Lider inimigo, e armadilhas de Pressao ficam mais fortes."
    ),
    PELE(
            "Pele",
            "Modo Defesa concede +40 DEF em vez de +25, e inimigos em Farm recebem +1 Pressao."
    ),
    CANARINHO(
            "Canarinho Pistola",
            "Se os 3 monstros atacarem no mesmo turno, todos recebem +15 ATK nesse combate."
    ),
    YUGI(
            "Yugi Muto",
            "Cada choque vencido aplica +1 Pressao extra no defensor."
    );

    private static final Map<String, LiderEnum> LEGACY_ALIASES = Map.of(
            "MAO", ASH,
            "KIM", TAI,
            "STALIN", PELE,
            "NAPOLEON", CANARINHO,
            "GENGHIS", YUGI
    );

    private final String nome;
    private final String passiva;

    LiderEnum(String nome, String passiva) {
        this.nome = nome;
        this.passiva = passiva;
    }

    public String getNome() {
        return nome;
    }

    public String getPassiva() {
        return passiva;
    }

    public static LiderEnum fromId(String id) {
        if (id == null || id.isBlank()) return null;

        String normalized = id.trim().toUpperCase(Locale.ROOT);
        LiderEnum legacy = LEGACY_ALIASES.get(normalized);
        if (legacy != null) return legacy;

        try {
            return LiderEnum.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public static String defaultId() {
        return ASH.name();
    }
}
