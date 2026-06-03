package com.team.multiversaltcg.game.engine;

import com.team.multiversaltcg.game.enums.LiderEnum;
import com.team.multiversaltcg.game.enums.EfeitoTrigger;
import com.team.multiversaltcg.game.enums.TipoEfeito;
import com.team.multiversaltcg.game.model.CampoBatalha;
import com.team.multiversaltcg.game.model.Carta;
import com.team.multiversaltcg.game.model.MonstroInstancia;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.model.ResultadoEfeitoDeclarativo;

import java.util.List;

public class GerenciadorArmadilha {

    private final ResolvedorEfeitoDeclarativo resolvedorDeclarativo =
            new ResolvedorEfeitoDeclarativo(new GerenciadorCompra(), new GerenciadorStatus());

    public record ResultadoArmadilha(boolean cancelarAtaque, boolean neutralizarTipo) {
        public static ResultadoArmadilha nenhuma() {
            return new ResultadoArmadilha(false, false);
        }
    }

    public void setar(CampoBatalha campo, Carta carta, boolean jogador, List<String> log) {
        if (campo.temArmadilhaAtiva(jogador)) {
            throw new RegraInvalidaException("Zona de armadilha ocupada.");
        }
        campo.setArmadilhaAtiva(jogador, carta.copy());
        log.add((jogador ? "Jogador" : "Inimigo") + " setou uma armadilha.");
    }

    public ResultadoArmadilha aoAtaque(CampoBatalha campo, boolean jogadorDefende,
                                       MonstroInstancia atacante, boolean vantagemTipo,
                                       List<String> log) {
        Carta armadilha = campo.getArmadilhaAtiva(jogadorDefende);
        if (armadilha == null || armadilha.getTrigger() == null) return ResultadoArmadilha.nenhuma();
        if (!armadilha.getTrigger().disparaAoAtaque()
                && !(vantagemTipo && armadilha.getTrigger().disparaAoUsarVantagemTipo())) {
            return ResultadoArmadilha.nenhuma();
        }

        descartar(campo, jogadorDefende, armadilha);
        log.add((jogadorDefende ? "Jogador" : "Inimigo") + " ativou armadilha: " + armadilha.getNome() + ".");

        ResultadoEfeitoDeclarativo custom = resolvedorDeclarativo.aplicar(campo, armadilha,
                EfeitoTrigger.AO_ARMADILHA_DISPARAR, jogadorDefende, null, atacante, null, null, log);

        if ("contra_ataque".equals(armadilha.getId())) {
            int pressao = getLider(campo, jogadorDefende).getTipo() == LiderEnum.TAI ? 2 : 1;
            atacante.adicionarPressao(pressao);
        } else if (armadilha.getEfeito() == TipoEfeito.PRESSAO_ALVO) {
            atacante.adicionarPressao(armadilha.getValor());
        }

        boolean cancelar = armadilha.getEfeito() == TipoEfeito.CANCELAR_ATAQUE || custom.cancelarAtaque();
        boolean neutralizar = (armadilha.getEfeito() == TipoEfeito.BARREIRA_TIPO && vantagemTipo)
                || custom.neutralizarTipo();
        return new ResultadoArmadilha(cancelar, neutralizar);
    }

    public void aoInvocar(CampoBatalha campo, boolean jogadorDonoArmadilha,
                          MonstroInstancia invocado, List<String> log) {
        Carta armadilha = campo.getArmadilhaAtiva(jogadorDonoArmadilha);
        if (armadilha == null || armadilha.getTrigger() == null || !armadilha.getTrigger().disparaAoInvocar()) {
            return;
        }

        descartar(campo, jogadorDonoArmadilha, armadilha);
        int pressao = getLider(campo, jogadorDonoArmadilha).getTipo() == LiderEnum.TAI ? 2 : armadilha.getValor();
        if (armadilha.getEfeito() == TipoEfeito.PRESSAO_ALVO || "armadilha_explosiva".equals(armadilha.getId())) {
            invocado.adicionarPressao(Math.max(1, pressao));
        }
        resolvedorDeclarativo.aplicar(campo, armadilha, EfeitoTrigger.AO_ARMADILHA_DISPARAR,
                jogadorDonoArmadilha, null, null, null, invocado, log);
        log.add((jogadorDonoArmadilha ? "Jogador" : "Inimigo") + " ativou armadilha: " + armadilha.getNome() + ".");
    }

    public boolean aoJogarMagia(CampoBatalha campo, boolean jogadorDonoArmadilha,
                                Carta magia, List<String> log) {
        Carta armadilha = campo.getArmadilhaAtiva(jogadorDonoArmadilha);
        if (armadilha == null || armadilha.getTrigger() == null || !armadilha.getTrigger().disparaAoJogarMagia()) {
            return false;
        }
        descartar(campo, jogadorDonoArmadilha, armadilha);
        ResultadoEfeitoDeclarativo custom = resolvedorDeclarativo.aplicar(campo, armadilha,
                EfeitoTrigger.AO_ARMADILHA_DISPARAR, jogadorDonoArmadilha, null, null, null, null, log);
        boolean anular = armadilha.getEfeito() == TipoEfeito.ESPELHO_MAGICO || custom.cancelarAtaque();
        if (!anular) {
            log.add((jogadorDonoArmadilha ? "Jogador" : "Inimigo") + " ativou "
                    + armadilha.getNome() + " contra " + magia.getNome() + ".");
            return false;
        }
        if (custom.cancelarAtaque() && armadilha.getEfeito() != TipoEfeito.ESPELHO_MAGICO) {
            log.add((jogadorDonoArmadilha ? "Jogador" : "Inimigo") + " ativou "
                    + armadilha.getNome() + " contra " + magia.getNome() + ".");
            return true;
        }
        log.add((jogadorDonoArmadilha ? "Jogador" : "Inimigo") + " ativou "
                + armadilha.getNome() + " e anulou " + magia.getNome() + ".");
        return true;
    }

    public void antesDoKO(CampoBatalha campo, List<String> log) {
        acionarJulgamento(campo, true, log);
        acionarJulgamento(campo, false, log);
    }

    private void acionarJulgamento(CampoBatalha campo, boolean jogadorDonoArmadilha, List<String> log) {
        Carta armadilha = campo.getArmadilhaAtiva(jogadorDonoArmadilha);
        if (armadilha == null || armadilha.getTrigger() == null || !armadilha.getTrigger().disparaAoKO()) return;

        MonstroInstancia[] aliados = jogadorDonoArmadilha ? campo.getSlotsJogador() : campo.getSlotsInimigo();
        boolean temAliadoKo = false;
        for (MonstroInstancia aliado : aliados) {
            if (aliado != null && aliado.isNocauteado()) {
                temAliadoKo = true;
                break;
            }
        }
        if (!temAliadoKo) return;

        MonstroInstancia[] inimigos = jogadorDonoArmadilha ? campo.getSlotsInimigo() : campo.getSlotsJogador();
        MonstroInstancia alvo = escolherMaiorPressao(inimigos);
        if (alvo == null) return;

        descartar(campo, jogadorDonoArmadilha, armadilha);
        if (armadilha.getEfeito() == TipoEfeito.JULGAMENTO || "julgamento".equals(armadilha.getId())) {
            alvo.adicionarPressao(2);
        }
        resolvedorDeclarativo.aplicar(campo, armadilha, EfeitoTrigger.AO_ARMADILHA_DISPARAR,
                jogadorDonoArmadilha, null, null, alvo, null, log);
        log.add((jogadorDonoArmadilha ? "Jogador" : "Inimigo") + " ativou Julgamento contra " + alvo.getNome() + ".");
    }

    private MonstroInstancia escolherMaiorPressao(MonstroInstancia[] slots) {
        MonstroInstancia alvo = null;
        for (MonstroInstancia m : slots) {
            if (m != null && (alvo == null || m.getPressure() > alvo.getPressure())) {
                alvo = m;
            }
        }
        return alvo;
    }

    private com.team.multiversaltcg.game.model.Lider getLider(CampoBatalha campo, boolean jogador) {
        return jogador ? campo.getLiderJogador() : campo.getLiderInimigo();
    }

    private void descartar(CampoBatalha campo, boolean jogador, Carta armadilha) {
        campo.setArmadilhaAtiva(jogador, null);
        campo.getDescarte(jogador).add(armadilha);
    }
}
