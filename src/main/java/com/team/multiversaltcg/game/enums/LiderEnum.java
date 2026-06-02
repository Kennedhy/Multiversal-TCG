package com.team.multiversaltcg.game.enums;

public enum LiderEnum {

    MAO(
            "Mao Tse-Tung",
            "Monstros em Modo Farm geram +2 Aura extra.",
            "Todos os monstros atacam e farmam ao mesmo tempo neste turno."
    ),
    KIM(
            "Kim Jong-un",
            "Modo Defesa que bloquear causa 8 HP direto ao Lider inimigo.",
            "Forca todos os inimigos a usar Modo Defesa no proximo turno."
    ),
    STALIN(
            "J. Stalin",
            "Modo Defesa concede +40 DEF em vez de +25.",
            "Escolhe 1 monstro inimigo e prende em Modo Defesa por 2 turnos."
    ),
    NAPOLEON(
            "Napoleao",
            "Se os 3 monstros atacarem no mesmo turno: +15 ATK para todos.",
            "Cada monstro seu ataca 2 vezes neste turno."
    ),
    GENGHIS(
            "Gengis Khan",
            "Cada choque vencido aplica +1 Pressao extra no defensor.",
            "Todos os monstros atacam todos os 3 inimigos por 50% do ATK (9 choques)."
    );

    private final String nome;
    private final String passiva;
    private final String especial;

    LiderEnum(String nome, String passiva, String especial) {
        this.nome = nome;
        this.passiva = passiva;
        this.especial = especial;
    }

    public String getNome() {
        return nome;
    }

    public String getPassiva() {
        return passiva;
    }

    public String getEspecial() {
        return especial;
    }
}
