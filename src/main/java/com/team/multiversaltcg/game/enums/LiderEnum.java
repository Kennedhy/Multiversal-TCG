package com.team.multiversaltcg.game.enums;

public enum LiderEnum {

    MAO(
            "Pikachu",
            "Monstros em Modo Farm geram +2 Aura extra."
    ),
    KIM(
            "Garurumon",
            "Bloqueios em Modo Defesa causam 8 HP direto ao Lider inimigo, e armadilhas de Pressao ficam mais fortes."
    ),
    STALIN(
            "Zagueiro Muro",
            "Modo Defesa concede +40 DEF em vez de +25, e inimigos em Farm recebem +1 Pressao."
    ),
    NAPOLEON(
            "O Rei",
            "Se os 3 monstros atacarem no mesmo turno, todos recebem +15 ATK nesse combate."
    ),
    GENGHIS(
            "Mago Negro",
            "Cada choque vencido aplica +1 Pressao extra no defensor."
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
}
