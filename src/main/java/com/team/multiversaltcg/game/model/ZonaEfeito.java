package com.team.multiversaltcg.game.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZonaEfeito {

    private Carta carta;
    private boolean viradaParaBaixo;

    public static ZonaEfeito armadilhaSetada(Carta carta) {
        return ZonaEfeito.builder()
                .carta(carta)
                .viradaParaBaixo(true)
                .build();
    }

    public static ZonaEfeito aberta(Carta carta) {
        return ZonaEfeito.builder()
                .carta(carta)
                .viradaParaBaixo(false)
                .build();
    }
}
