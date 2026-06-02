package com.team.multiversaltcg.game.engine;

import com.team.multiversaltcg.game.enums.EfeitoAcaoTipo;
import com.team.multiversaltcg.game.enums.EfeitoAlvo;
import com.team.multiversaltcg.game.enums.EfeitoTrigger;
import com.team.multiversaltcg.game.model.AcaoEfeitoTurno;
import com.team.multiversaltcg.game.model.CampoBatalha;
import com.team.multiversaltcg.game.model.Carta;
import com.team.multiversaltcg.game.model.EfeitoAcaoDeclarativa;
import com.team.multiversaltcg.game.model.EfeitoRegraDeclarativa;
import com.team.multiversaltcg.game.model.Lider;
import com.team.multiversaltcg.game.model.MonstroInstancia;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.model.ResultadoEfeitoDeclarativo;

import java.util.ArrayList;
import java.util.List;

public class ResolvedorEfeitoDeclarativo {

    private final GerenciadorCompra gerenciadorCompra;
    private final GerenciadorStatus gerenciadorStatus;

    public ResolvedorEfeitoDeclarativo(GerenciadorCompra gerenciadorCompra, GerenciadorStatus gerenciadorStatus) {
        this.gerenciadorCompra = gerenciadorCompra;
        this.gerenciadorStatus = gerenciadorStatus;
    }

    public boolean temTrigger(Carta carta, EfeitoTrigger trigger) {
        if (carta == null || carta.getRegras() == null) return false;
        return carta.getRegras().stream().anyMatch(r -> r.getTrigger() == trigger);
    }

    public boolean precisaAlvoAoJogar(Carta carta) {
        if (carta == null || carta.getRegras() == null) return false;
        return carta.getRegras().stream()
                .filter(r -> r.getTrigger() == EfeitoTrigger.AO_JOGAR)
                .anyMatch(r -> r.getTarget() == EfeitoAlvo.ALLY_TARGET || r.getTarget() == EfeitoAlvo.ENEMY_TARGET);
    }

    public String targetingModeAoJogar(Carta carta) {
        if (carta == null || carta.getRegras() == null) return "NONE";
        for (EfeitoRegraDeclarativa regra : carta.getRegras()) {
            if (regra.getTrigger() != EfeitoTrigger.AO_JOGAR) continue;
            if (regra.getTarget() == EfeitoAlvo.ALLY_TARGET) return "OWN";
            if (regra.getTarget() == EfeitoAlvo.ENEMY_TARGET) return "ENEMY";
        }
        return "NONE";
    }

    public ResultadoEfeitoDeclarativo aplicar(CampoBatalha campo, Carta carta, EfeitoTrigger trigger,
                                               boolean jogadorDono, AcaoEfeitoTurno acao,
                                               MonstroInstancia atacante, MonstroInstancia defensor,
                                               MonstroInstancia invocado, List<String> log) {
        if (carta == null || carta.getRegras() == null || carta.getRegras().isEmpty()) {
            return ResultadoEfeitoDeclarativo.nenhum();
        }

        ResultadoEfeitoDeclarativo resultado = ResultadoEfeitoDeclarativo.nenhum();
        for (EfeitoRegraDeclarativa regra : carta.getRegras()) {
            if (regra.getTrigger() != trigger) continue;
            if (regra.getTipoAlvo() != null && trigger == EfeitoTrigger.CONTINUO_FIM_TURNO) {
                // O filtro por tipo e aplicado por alvo abaixo, mantendo a regra declarativa simples.
            }
            List<MonstroInstancia> alvos = resolverAlvos(campo, regra, jogadorDono, acao, atacante, defensor, invocado);
            for (EfeitoAcaoDeclarativa action : safeActions(regra)) {
                resultado = resultado.merge(aplicarAcao(campo, carta, action, jogadorDono, alvos, log));
            }
        }
        return resultado;
    }

    private ResultadoEfeitoDeclarativo aplicarAcao(CampoBatalha campo, Carta carta, EfeitoAcaoDeclarativa action,
                                                   boolean jogadorDono, List<MonstroInstancia> alvos,
                                                   List<String> log) {
        if (action == null || action.getTipo() == null) return ResultadoEfeitoDeclarativo.nenhum();
        int valor = Math.max(0, action.getValor());
        switch (action.getTipo()) {
            case AURA -> campo.adicionarAura(jogadorDono, valor);
            case CURA_LIDER -> curarLider(campo, jogadorDono, valor);
            case COMPRA -> comprar(campo, jogadorDono, valor, log);
            case PRESSAO -> alvos.forEach(m -> m.adicionarPressao(Math.max(1, valor)));
            case CURA_PRESSAO -> alvos.forEach(m -> m.curarPressao(Math.max(1, valor)));
            case BUFF_ATK -> alvos.forEach(m -> m.setAtkBuff(m.getAtkBuff() + valor));
            case BUFF_DEF -> alvos.forEach(m -> m.setDefBuff(m.getDefBuff() + valor));
            case APLICAR_STATUS -> {
                if (action.getStatus() != null) {
                    int duracao = action.getDuracao() > 0 ? action.getDuracao() : action.getStatus().getDuracaoPadrao();
                    alvos.forEach(m -> gerenciadorStatus.aplicarStatus(m, action.getStatus(), duracao));
                }
            }
            case REMOVER_STATUS -> alvos.forEach(gerenciadorStatus::limparTodos);
            case IMUNIDADE -> alvos.forEach(m -> {
                m.setImune(true);
                m.setImunoTurnos(Math.max(m.getImunoTurnos(), Math.max(1, action.getDuracao())));
            });
            case IGNORAR_DEFESA -> alvos.forEach(m -> m.setIgnorarDefBuffTurnos(Math.max(1, action.getDuracao())));
            case CANCELAR_ATAQUE -> {
                log.add(carta.getNome() + " cancelou o ataque.");
                return new ResultadoEfeitoDeclarativo(true, false);
            }
            case NEUTRALIZAR_TIPO -> {
                log.add(carta.getNome() + " neutralizou a vantagem de tipo.");
                return new ResultadoEfeitoDeclarativo(false, true);
            }
        }
        if (!alvos.isEmpty() || action.getTipo() == EfeitoAcaoTipo.AURA || action.getTipo() == EfeitoAcaoTipo.CURA_LIDER
                || action.getTipo() == EfeitoAcaoTipo.COMPRA) {
            log.add(carta.getNome() + " aplicou efeito custom: " + action.getTipo() + ".");
        }
        return ResultadoEfeitoDeclarativo.nenhum();
    }

    private List<MonstroInstancia> resolverAlvos(CampoBatalha campo, EfeitoRegraDeclarativa regra, boolean jogadorDono,
                                                 AcaoEfeitoTurno acao, MonstroInstancia atacante,
                                                 MonstroInstancia defensor, MonstroInstancia invocado) {
        EfeitoAlvo target = regra.getTarget() == null ? EfeitoAlvo.NONE : regra.getTarget();
        List<MonstroInstancia> alvos = new ArrayList<>();
        switch (target) {
            case NONE -> {
            }
            case ALLY_TARGET -> alvos.add(getAlvoAliado(campo, jogadorDono, acao));
            case ENEMY_TARGET -> alvos.add(getAlvoInimigo(campo, jogadorDono, acao));
            case ALL_ALLIES -> addFiltrados(alvos, jogadorDono ? campo.getSlotsJogador() : campo.getSlotsInimigo(), regra);
            case ALL_ENEMIES -> addFiltrados(alvos, jogadorDono ? campo.getSlotsInimigo() : campo.getSlotsJogador(), regra);
            case ATTACKER -> addSeValido(alvos, atacante, regra);
            case DEFENDER -> addSeValido(alvos, defensor, regra);
            case SUMMONED -> addSeValido(alvos, invocado, regra);
            case MOST_PRESSURED_ENEMY -> addSeValido(alvos, maisPressionado(jogadorDono ? campo.getSlotsInimigo() : campo.getSlotsJogador()), regra);
        }
        return alvos;
    }

    private MonstroInstancia getAlvoAliado(CampoBatalha campo, boolean jogadorDono, AcaoEfeitoTurno acao) {
        if (acao == null) throw new RegraInvalidaException("Alvo aliado do efeito custom e obrigatorio.");
        MonstroInstancia[] slots = jogadorDono ? campo.getSlotsJogador() : campo.getSlotsInimigo();
        int slot = validarSlot(acao.getSlotMonstroAlvo());
        if (slots[slot] == null) throw new RegraInvalidaException("Alvo aliado vazio.");
        return slots[slot];
    }

    private MonstroInstancia getAlvoInimigo(CampoBatalha campo, boolean jogadorDono, AcaoEfeitoTurno acao) {
        if (acao == null) throw new RegraInvalidaException("Alvo inimigo do efeito custom e obrigatorio.");
        MonstroInstancia[] slots = jogadorDono ? campo.getSlotsInimigo() : campo.getSlotsJogador();
        int slot = validarSlot(acao.getSlotAlvo());
        if (slots[slot] == null) throw new RegraInvalidaException("Alvo inimigo vazio.");
        return slots[slot];
    }

    private void addFiltrados(List<MonstroInstancia> alvos, MonstroInstancia[] slots, EfeitoRegraDeclarativa regra) {
        for (MonstroInstancia m : slots) addSeValido(alvos, m, regra);
    }

    private void addSeValido(List<MonstroInstancia> alvos, MonstroInstancia m, EfeitoRegraDeclarativa regra) {
        if (m == null) return;
        if (regra.getTipoAlvo() != null && m.getTipo() != regra.getTipoAlvo()) return;
        alvos.add(m);
    }

    private MonstroInstancia maisPressionado(MonstroInstancia[] slots) {
        MonstroInstancia alvo = null;
        for (MonstroInstancia m : slots) {
            if (m != null && (alvo == null || m.getPressure() > alvo.getPressure())) alvo = m;
        }
        return alvo;
    }

    private void curarLider(CampoBatalha campo, boolean jogador, int valor) {
        Lider lider = jogador ? campo.getLiderJogador() : campo.getLiderInimigo();
        lider.setHp(Math.min(lider.getHpMaximo(), lider.getHp() + valor));
    }

    private void comprar(CampoBatalha campo, boolean jogador, int valor, List<String> log) {
        for (int i = 0; i < valor; i++) {
            gerenciadorCompra.comprar(campo, jogador, log, true);
            if (campo.isJogoEncerrado()) return;
        }
    }

    private List<EfeitoAcaoDeclarativa> safeActions(EfeitoRegraDeclarativa regra) {
        return regra.getActions() == null ? List.of() : regra.getActions();
    }

    private int validarSlot(int slot) {
        if (slot < 0 || slot > 2) throw new RegraInvalidaException("Slot de alvo custom invalido: " + slot);
        return slot;
    }
}
