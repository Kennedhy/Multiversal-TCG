package com.team.multiversaltcg.game.model;

import com.team.multiversaltcg.game.enums.EfeitoAlvo;
import com.team.multiversaltcg.game.enums.EfeitoTrigger;
import com.team.multiversaltcg.game.enums.TipoUniversal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EfeitoRegraDeclarativa {

    private EfeitoTrigger trigger;
    private EfeitoAlvo target;
    private TipoUniversal tipoAlvo;
    private List<EfeitoAcaoDeclarativa> actions;
}
