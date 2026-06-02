package com.team.multiversaltcg.game.model;

import com.team.multiversaltcg.game.enums.EfeitoAcaoTipo;
import com.team.multiversaltcg.game.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EfeitoAcaoDeclarativa {

    private EfeitoAcaoTipo tipo;
    private int valor;
    private int duracao;
    private StatusEnum status;
}
