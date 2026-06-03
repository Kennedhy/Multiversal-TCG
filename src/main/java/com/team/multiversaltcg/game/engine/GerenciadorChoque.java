package com.team.multiversaltcg.game.engine;

import com.team.multiversaltcg.game.enums.LiderEnum;
import com.team.multiversaltcg.game.enums.ModoAcao;
import com.team.multiversaltcg.game.enums.ResultadoChoqueEnum;
import com.team.multiversaltcg.game.model.Ataque;
import com.team.multiversaltcg.game.model.Lider;
import com.team.multiversaltcg.game.model.MonstroInstancia;
import com.team.multiversaltcg.game.model.ResultadoChoque;

public class GerenciadorChoque {

    private final GerenciadorStatus gerenciadorStatus;

    public GerenciadorChoque(GerenciadorStatus gerenciadorStatus) {
        this.gerenciadorStatus = gerenciadorStatus;
    }

    public ResultadoChoque resolver(
            MonstroInstancia atacante,
            Ataque ataque,
            MonstroInstancia defensor,
            Lider liderAtacante,
            Lider liderDefensor) {
        return resolver(atacante, ataque, defensor, liderAtacante, liderDefensor, false, false);
    }

    public ResultadoChoque resolver(
            MonstroInstancia atacante,
            Ataque ataque,
            MonstroInstancia defensor,
            Lider liderAtacante,
            Lider liderDefensor,
            boolean multiplicadorNeutro,
            boolean ignorarDefesa) {

        boolean defensorEmDefesa = defensor.getModoAtual() == ModoAcao.DEFESA;
        int bonusDefLider = defensorEmDefesa ? liderDefensor.getBonusDef() - 25 : 0;
        double multiplicador = multiplicadorNeutro ? 1.0 : atacante.getTipo().getMultiplicador(defensor.getTipo());

        int atkBase = ataque.getAtkTotal(atacante.getAtkTotal());
        int atkEfetivo = (int) Math.round(atkBase * multiplicador);
        int defEfetiva = ignorarDefesa ? defensor.getDefBase() : defensor.getDefTotal(defensorEmDefesa, bonusDefLider);

        ResultadoChoqueEnum resultado;
        int pressaoAtacante = 0;
        int pressaoDefensor = 0;

        if (atkEfetivo > defEfetiva) {
            resultado = ResultadoChoqueEnum.VITORIA;
            pressaoDefensor = calcularPressaoVitoria(liderAtacante);
        } else if (atkEfetivo < defEfetiva) {
            resultado = ResultadoChoqueEnum.DERROTA;
            pressaoAtacante = 1;
        } else {
            resultado = ResultadoChoqueEnum.EMPATE;
        }

        if (resultado == ResultadoChoqueEnum.VITORIA && ataque.temStatus()) {
            gerenciadorStatus.aplicarStatus(defensor, ataque.getStatusAplicado(),
                    ataque.getDuracaoStatus());
        }

        if (resultado == ResultadoChoqueEnum.VITORIA && multiplicador > 1.0) {
            aplicarRivalidade(atacante, defensor);
        }

        int danoLiderBloqueio = 0;
        if (resultado == ResultadoChoqueEnum.DERROTA
                && defensorEmDefesa
                && liderDefensor.getTipo() == LiderEnum.TAI) {
            danoLiderBloqueio = 8;
        }

        int auraAbsorvida = 0;
        if (resultado == ResultadoChoqueEnum.VITORIA) {
            auraAbsorvida = multiplicador > 1.0 ? 2 : 1;
        }
        if (resultado == ResultadoChoqueEnum.DERROTA && defensorEmDefesa) {
            auraAbsorvida = 1;
        }

        atacante.adicionarPressao(pressaoAtacante);
        defensor.adicionarPressao(pressaoDefensor);

        return ResultadoChoque.builder()
                .resultado(resultado)
                .nomeAtacante(atacante.getNome())
                .nomeDefensor(defensor.getNome())
                .idAtacante(atacante.getId())
                .idDefensor(defensor.getId())
                .atkEfetivo(atkEfetivo)
                .defEfetiva(defEfetiva)
                .multiplicadorTipo(multiplicador)
                .pressaoAplicadaAtacante(pressaoAtacante)
                .pressaoAplicadaDefensor(pressaoDefensor)
                .statusAplicado(ataque.temStatus() && resultado == ResultadoChoqueEnum.VITORIA
                        ? ataque.getStatusAplicado()
                        : null)
                .duracaoStatus(ataque.getDuracaoStatus())
                .bloqueioDefesa(defensorEmDefesa && resultado == ResultadoChoqueEnum.DERROTA)
                .danoLiderBloqueio(danoLiderBloqueio)
                .auraAbsorvida(auraAbsorvida)
                .descricaoLog(montarLog(atacante, defensor, atkEfetivo, defEfetiva,
                        multiplicador, resultado, ataque))
                .build();
    }

    private int calcularPressaoVitoria(Lider liderAtacante) {
        if (liderAtacante.getTipo() == LiderEnum.YUGI) return 2;
        return 1;
    }

    private void aplicarRivalidade(MonstroInstancia atacante, MonstroInstancia defensor) {
        switch (atacante.getTipo()) {
            case CHAMA -> gerenciadorStatus.aplicarStatus(defensor, com.team.multiversaltcg.game.enums.StatusEnum.QUEIMADO, 2);
            case ABISMO -> defensor.setAtkBuff(defensor.getAtkBuff() - 12);
            case NATUREZA -> atacante.curarPressao(1);
            case RELAMPAGO -> atacante.setAtkBuff(atacante.getAtkBuff() + 4);
            case SOMBRA -> defensor.setTurnosPressoDef(Math.max(defensor.getTurnosPressoDef(), 1));
            case ETER -> defensor.setDefBuff(Math.max(0, defensor.getDefBuff() - 10));
        }
    }

    private String montarLog(MonstroInstancia atacante, MonstroInstancia defensor,
                             int atk, int def, double mult,
                             ResultadoChoqueEnum resultado, Ataque ataque) {
        String multStr = mult != 1.0 ? " (x" + mult + ")" : "";
        String res = switch (resultado) {
            case VITORIA -> "Vitoria";
            case DERROTA -> "Derrota";
            case EMPATE -> "Empate";
        };
        return res + " - " + atacante.getNome() + " [" + ataque.getNome() + "]"
                + " ATK " + atk + multStr + " vs " + defensor.getNome()
                + " DEF " + def;
    }
}
