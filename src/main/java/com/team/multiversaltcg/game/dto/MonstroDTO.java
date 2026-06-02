package com.team.multiversaltcg.game.dto;

import com.team.multiversaltcg.game.enums.ModoAcao;
import com.team.multiversaltcg.game.model.MonstroInstancia;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MonstroDTO {

    private String id;
    private String nome;
    private String imageUrl;
    private String tipo;
    private String universo;
    private int atk;
    private int def;
    private int pressure;
    private String modoAtual;
    private boolean nocauteado;
    private boolean imune;
    private List<String> statusAtivos;
    private List<AtaqueDTO> ataques;

    public static MonstroDTO from(MonstroInstancia m) {
        if (m == null) return null;

        List<String> status = m.getStatusAtivos().toList()
                .stream()
                .map(s -> s.getTipo().getNome()
                        + " (" + s.getTurnosRestantes() + "t)")
                .toList();

        List<AtaqueDTO> ataques = m.getTemplate().getAtaques()
                .stream()
                .map(AtaqueDTO::from)
                .toList();

        return MonstroDTO.builder()
                .id(m.getId())
                .nome(m.getNome())
                .imageUrl(m.getImageUrl())
                .tipo(m.getTipo().getNome())
                .universo(m.getUniverso())
                .atk(m.getAtkTotal())
                .def(m.getDefTotal(
                        m.getModoAtual() == ModoAcao.DEFESA, 0))
                .pressure(m.getPressure())
                .modoAtual(m.getModoAtual().getNome())
                .nocauteado(m.isNocauteado())
                .imune(m.isImune())
                .statusAtivos(status)
                .ataques(ataques)
                .build();
    }
}
