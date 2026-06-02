package com.team.multiversaltcg.game.enums;

public enum TipoUniversal {

    CHAMA("Chama", 3),
    ABISMO("Abismo", 2),
    NATUREZA("Natureza", 2),
    RELAMPAGO("Relampago", 4),
    SOMBRA("Sombra", 3),
    ETER("Eter", 2);

    private final String nome;
    private final int auraFarm;

    TipoUniversal(String nome, int auraFarm) {
        this.nome = nome;
        this.auraFarm = auraFarm;
    }

    public String getNome() {
        return nome;
    }

    public int getAuraFarm() {
        return auraFarm;
    }

    public double getMultiplicador(TipoUniversal defensor) {
        if (getVantagens()[0] == defensor || getVantagens()[1] == defensor) return 1.5;
        if (getDesvantagens()[0] == defensor || getDesvantagens()[1] == defensor) return 0.6;
        return 1.0;
    }

    private TipoUniversal[] getVantagens() {
        return switch (this) {
            case CHAMA     -> new TipoUniversal[]{NATUREZA, SOMBRA};
            case ABISMO    -> new TipoUniversal[]{CHAMA, RELAMPAGO};
            case NATUREZA  -> new TipoUniversal[]{ABISMO, ETER};
            case RELAMPAGO -> new TipoUniversal[]{CHAMA, SOMBRA};
            case SOMBRA    -> new TipoUniversal[]{NATUREZA, ETER};
            case ETER      -> new TipoUniversal[]{ABISMO, RELAMPAGO};
        };
    }

    private TipoUniversal[] getDesvantagens() {
        return switch (this) {
            case CHAMA     -> new TipoUniversal[]{ABISMO, RELAMPAGO};
            case ABISMO    -> new TipoUniversal[]{NATUREZA, ETER};
            case NATUREZA  -> new TipoUniversal[]{CHAMA, SOMBRA};
            case RELAMPAGO -> new TipoUniversal[]{ABISMO, ETER};
            case SOMBRA    -> new TipoUniversal[]{CHAMA, RELAMPAGO};
            case ETER      -> new TipoUniversal[]{NATUREZA, SOMBRA};
        };
    }
}
