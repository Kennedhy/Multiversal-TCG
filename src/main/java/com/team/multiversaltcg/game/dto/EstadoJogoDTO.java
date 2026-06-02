package com.team.multiversaltcg.game.dto;

import com.team.multiversaltcg.game.model.CampoBatalha;
import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
@Builder
public class EstadoJogoDTO {

    private List<MonstroDTO> slotsJogador;
    private List<MonstroDTO> slotsInimigo;
    private List<CartaDTO> maoJogador;
    private List<CartaDTO> zonasEfeitoJogador;
    private List<CartaDTO> zonasEfeitoInimigo;
    private CartaDTO magiaAtivaJogador;
    private CartaDTO magiaAtivaInimigo;
    private CartaDTO armadilhaJogador;
    private CartaDTO armadilhaInimigo;
    private boolean armadilhaJogadorAtiva;
    private boolean armadilhaInimigoAtiva;

    private LiderDTO liderJogador;
    private LiderDTO liderInimigo;

    private int auraJogador;
    private int auraInimigo;

    private int turnoAtual;
    private String faseAtual;

    private boolean jogoEncerrado;
    private String vencedor;

    private List<String> log;

    private int tamanhoDeckJogador;
    private int tamanhoDeckInimigo;
    private int tamanhoMaoJogador;
    private int tamanhoMaoInimigo;
    private int tamanhoDescarteJogador;
    private int tamanhoDescarteInimigo;

    public static EstadoJogoDTO from(CampoBatalha campo, List<String> log) {
        List<MonstroDTO> slotsJ = Arrays.stream(campo.getSlotsJogador())
                .map(MonstroDTO::from)
                .toList();

        List<MonstroDTO> slotsI = Arrays.stream(campo.getSlotsInimigo())
                .map(MonstroDTO::from)
                .toList();

        List<CartaDTO> maoJogador = campo.getMaoJogador().stream()
                .map(CartaDTO::from)
                .toList();

        List<CartaDTO> zonasJogador = Arrays.stream(campo.getZonasEfeitoJogador())
                .map(z -> CartaDTO.fromZona(z, false))
                .toList();

        List<CartaDTO> zonasInimigo = Arrays.stream(campo.getZonasEfeitoInimigo())
                .map(z -> CartaDTO.fromZona(z, true))
                .toList();
        CartaDTO magiaJogador = CartaDTO.from(campo.getMagiaAtivaJogador());
        CartaDTO magiaInimigo = CartaDTO.from(campo.getMagiaAtivaInimigo());
        CartaDTO armadilhaJogador = campo.getArmadilhaAtivaJogador() == null
                ? null
                : CartaDTO.from(campo.getArmadilhaAtivaJogador());
        CartaDTO armadilhaInimigo = campo.getArmadilhaAtivaInimigo() == null
                ? null
                : CartaDTO.hidden();

        return EstadoJogoDTO.builder()
                .slotsJogador(slotsJ)
                .slotsInimigo(slotsI)
                .maoJogador(maoJogador)
                .zonasEfeitoJogador(zonasJogador)
                .zonasEfeitoInimigo(zonasInimigo)
                .magiaAtivaJogador(magiaJogador)
                .magiaAtivaInimigo(magiaInimigo)
                .armadilhaJogador(armadilhaJogador)
                .armadilhaInimigo(armadilhaInimigo)
                .armadilhaJogadorAtiva(campo.getArmadilhaAtivaJogador() != null)
                .armadilhaInimigoAtiva(campo.getArmadilhaAtivaInimigo() != null)
                .liderJogador(LiderDTO.from(campo.getLiderJogador()))
                .liderInimigo(LiderDTO.from(campo.getLiderInimigo()))
                .auraJogador(campo.getAuraJogador())
                .auraInimigo(campo.getAuraInimigo())
                .turnoAtual(campo.getTurnoAtual())
                .faseAtual(campo.getFaseAtual().getDescricao())
                .jogoEncerrado(campo.isJogoEncerrado())
                .vencedor(campo.getVencedor())
                .log(log)
                .tamanhoDeckJogador(campo.getDeckJogador().size())
                .tamanhoDeckInimigo(campo.getDeckInimigo().size())
                .tamanhoMaoJogador(campo.getMaoJogador().size())
                .tamanhoMaoInimigo(campo.getMaoInimigo().size())
                .tamanhoDescarteJogador(campo.getDescarteJogador().size())
                .tamanhoDescarteInimigo(campo.getDescarteInimigo().size())
                .build();
    }

    public static EstadoJogoDTO fromPerspective(CampoBatalha campo, List<String> log, boolean jogadorPerspective) {
        if (jogadorPerspective) {
            return from(campo, log);
        }

        List<MonstroDTO> slotsJ = Arrays.stream(campo.getSlotsInimigo())
                .map(MonstroDTO::from)
                .toList();

        List<MonstroDTO> slotsI = Arrays.stream(campo.getSlotsJogador())
                .map(MonstroDTO::from)
                .toList();

        List<CartaDTO> maoJogador = campo.getMaoInimigo().stream()
                .map(CartaDTO::from)
                .toList();

        List<CartaDTO> zonasJogador = Arrays.stream(campo.getZonasEfeitoInimigo())
                .map(z -> CartaDTO.fromZona(z, false))
                .toList();

        List<CartaDTO> zonasInimigo = Arrays.stream(campo.getZonasEfeitoJogador())
                .map(z -> CartaDTO.fromZona(z, true))
                .toList();

        CartaDTO armadilhaJogador = campo.getArmadilhaAtivaInimigo() == null
                ? null
                : CartaDTO.from(campo.getArmadilhaAtivaInimigo());
        CartaDTO armadilhaInimigo = campo.getArmadilhaAtivaJogador() == null
                ? null
                : CartaDTO.hidden();

        return EstadoJogoDTO.builder()
                .slotsJogador(slotsJ)
                .slotsInimigo(slotsI)
                .maoJogador(maoJogador)
                .zonasEfeitoJogador(zonasJogador)
                .zonasEfeitoInimigo(zonasInimigo)
                .magiaAtivaJogador(CartaDTO.from(campo.getMagiaAtivaInimigo()))
                .magiaAtivaInimigo(CartaDTO.from(campo.getMagiaAtivaJogador()))
                .armadilhaJogador(armadilhaJogador)
                .armadilhaInimigo(armadilhaInimigo)
                .armadilhaJogadorAtiva(campo.getArmadilhaAtivaInimigo() != null)
                .armadilhaInimigoAtiva(campo.getArmadilhaAtivaJogador() != null)
                .liderJogador(LiderDTO.from(campo.getLiderInimigo()))
                .liderInimigo(LiderDTO.from(campo.getLiderJogador()))
                .auraJogador(campo.getAuraInimigo())
                .auraInimigo(campo.getAuraJogador())
                .turnoAtual(campo.getTurnoAtual())
                .faseAtual(campo.getFaseAtual().getDescricao())
                .jogoEncerrado(campo.isJogoEncerrado())
                .vencedor(inverterVencedor(campo.getVencedor()))
                .log(log)
                .tamanhoDeckJogador(campo.getDeckInimigo().size())
                .tamanhoDeckInimigo(campo.getDeckJogador().size())
                .tamanhoMaoJogador(campo.getMaoInimigo().size())
                .tamanhoMaoInimigo(campo.getMaoJogador().size())
                .tamanhoDescarteJogador(campo.getDescarteInimigo().size())
                .tamanhoDescarteInimigo(campo.getDescarteJogador().size())
                .build();
    }

    private static String inverterVencedor(String vencedor) {
        if ("JOGADOR".equals(vencedor)) return "INIMIGO";
        if ("INIMIGO".equals(vencedor)) return "JOGADOR";
        return vencedor;
    }
}
