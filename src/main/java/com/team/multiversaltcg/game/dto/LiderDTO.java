package com.team.multiversaltcg.game.dto;

import com.team.multiversaltcg.game.model.Lider;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LiderDTO {

    private String id;
    private String nome;
    private int hp;
    private int hpMaximo;
    private String passiva;

    public static LiderDTO from(Lider lider) {
        return LiderDTO.builder()
                .id(lider.getTipo().name())
                .nome(lider.getTipo().getNome())
                .hp(lider.getHp())
                .hpMaximo(lider.getHpMaximo())
                .passiva(lider.getTipo().getPassiva())
                .build();
    }
}
