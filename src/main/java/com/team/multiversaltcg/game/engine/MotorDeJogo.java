package com.team.multiversaltcg.game.engine;

import com.team.multiversaltcg.game.enums.CardType;
import com.team.multiversaltcg.game.enums.EfeitoTrigger;
import com.team.multiversaltcg.game.enums.FaseEnum;
import com.team.multiversaltcg.game.enums.LiderEnum;
import com.team.multiversaltcg.game.enums.ModoAcao;
import com.team.multiversaltcg.game.enums.StatusEnum;
import com.team.multiversaltcg.game.model.AcaoEfeitoTurno;
import com.team.multiversaltcg.game.model.AcaoTurno;
import com.team.multiversaltcg.game.model.Ataque;
import com.team.multiversaltcg.game.model.CampoBatalha;
import com.team.multiversaltcg.game.model.Carta;
import com.team.multiversaltcg.game.model.InvocacaoTurno;
import com.team.multiversaltcg.game.model.Lider;
import com.team.multiversaltcg.game.model.MonstroInstancia;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.model.ResultadoChoque;
import com.team.multiversaltcg.game.model.TurnoJogador;
import com.team.multiversaltcg.game.service.CartaDataService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MotorDeJogo {

    private final GerenciadorChoque gerenciadorChoque;
    private final GerenciadorAura gerenciadorAura;
    private final GerenciadorStatus gerenciadorStatus;
    private final GerenciadorKO gerenciadorKO;
    private final GerenciadorLider gerenciadorLider;
    private final GerenciadorCompra gerenciadorCompra;
    private final GerenciadorEvolucao gerenciadorEvolucao;
    private final GerenciadorMagia gerenciadorMagia;
    private final GerenciadorArmadilha gerenciadorArmadilha;
    private final ResolvedorEfeitoDeclarativo resolvedorDeclarativo;
    private final IAInimiga ia;

    private CampoBatalha campo;
    private List<String> logTurno;
    private boolean jogadorEvoluiuNoTurno;
    private boolean inimigoEvoluiuNoTurno;

    public MotorDeJogo(CartaDataService cartaDataService) {
        this.gerenciadorStatus = new GerenciadorStatus();
        this.gerenciadorChoque = new GerenciadorChoque(gerenciadorStatus);
        this.gerenciadorAura = new GerenciadorAura();
        this.gerenciadorKO = new GerenciadorKO();
        this.gerenciadorLider = new GerenciadorLider();
        this.gerenciadorCompra = new GerenciadorCompra();
        this.gerenciadorEvolucao = new GerenciadorEvolucao(cartaDataService);
        this.gerenciadorMagia = new GerenciadorMagia(gerenciadorCompra, gerenciadorStatus);
        this.gerenciadorArmadilha = new GerenciadorArmadilha();
        this.resolvedorDeclarativo = new ResolvedorEfeitoDeclarativo(gerenciadorCompra, gerenciadorStatus);
        this.ia = new IAInimiga();
    }

    public void iniciarPartida(CampoBatalha campoBatalha) {
        this.campo = campoBatalha;
        this.logTurno = new ArrayList<>();
        log("Partida iniciada. Turno 1.");
    }

    public List<String> processarTurno(TurnoJogador turnoJogador) {
        logTurno = new ArrayList<>();
        jogadorEvoluiuNoTurno = false;
        inimigoEvoluiuNoTurno = false;
        if (campo.isJogoEncerrado()) return logTurno;
        TurnoJogador turno = turnoJogador == null ? new TurnoJogador() : turnoJogador;

        campo.setFaseAtual(FaseEnum.ATRIBUICAO);
        processarInvocacao(turno.getInvocacaoMonstro(), true);
        ajustarIndiceEfeitoAposInvocacao(turno);
        processarEfeito(turno.getAcaoEfeito(), true);

        processarEvolucaoIA();
        processarInvocacaoIA();
        processarEfeitoIA();

        List<AcaoTurno> acoesJogador = turno.getAcoesCombate();
        List<AcaoTurno> acoesIA = ia.decidirAcoes(campo);

        gerenciadorLider.forcarModosPresos(campo);
        aplicarModos(campo.getSlotsJogador(), acoesJogador, true);
        aplicarModos(campo.getSlotsInimigo(), acoesIA, false);

        campo.setFaseAtual(FaseEnum.FARM);
        gerenciadorAura.aplicarAuraBase(campo);
        log("+3 Aura base para ambos.");

        if (campo.getLiderJogador().isMaoEspecialAtivo()) {
            gerenciadorAura.aplicarFarmMaoTudoAtacaEFarma(campo);
            log("Especial Mao: monstros farmam e atacam neste turno.");
        } else {
            gerenciadorAura.resolverFarmJogador(campo);
        }
        gerenciadorAura.resolverFarmInimigo(campo);
        gerenciadorLider.aplicarPassivaGulag(campo, true);
        gerenciadorLider.aplicarPassivaGulag(campo, false);

        gerenciadorMagia.resolverContinuos(campo, logTurno);
        processarKO();
        if (campo.isJogoEncerrado()) return logTurno;

        campo.setFaseAtual(FaseEnum.ATAQUE);
        aplicarBonusLiderEAtacar(acoesJogador, true, true);
        resolverAtaquesIA(acoesIA);

        processarKO();
        if (campo.isJogoEncerrado()) return logTurno;

        campo.setFaseAtual(FaseEnum.STATUS);
        gerenciadorStatus.resolverTodosMonstros(campo.getSlotsJogador());
        gerenciadorStatus.resolverTodosMonstros(campo.getSlotsInimigo());
        processarKO();
        if (campo.isJogoEncerrado()) return logTurno;

        campo.setFaseAtual(FaseEnum.FIM_TURNO);
        gerenciadorLider.decrementarModosPresos(campo);
        campo.getLiderJogador().limparFlagsEspeciais();
        campo.getLiderInimigo().limparFlagsEspeciais();

        campo.setTurnoAtual(campo.getTurnoAtual() + 1);
        comprarParaProximoTurno();
        if (campo.isJogoEncerrado()) return logTurno;
        campo.setFaseAtual(FaseEnum.ATRIBUICAO);
        log("Turno " + campo.getTurnoAtual() + " iniciado.");
        return logTurno;
    }

    public List<String> processarTurnoPvp(TurnoJogador turnoJogador,
                                          TurnoJogador turnoInimigo,
                                          boolean jogadorPrimeiro,
                                          boolean especialJogador,
                                          boolean especialInimigo) {
        logTurno = new ArrayList<>();
        jogadorEvoluiuNoTurno = false;
        inimigoEvoluiuNoTurno = false;
        if (campo.isJogoEncerrado()) return logTurno;

        TurnoJogador jogador = turnoJogador == null ? new TurnoJogador() : turnoJogador;
        TurnoJogador inimigo = turnoInimigo == null ? new TurnoJogador() : turnoInimigo;

        if (especialJogador) ativarEspecial(true);
        if (especialInimigo) ativarEspecial(false);

        campo.setFaseAtual(FaseEnum.ATRIBUICAO);
        processarAtribuicaoPvp(jogadorPrimeiro, jogador, inimigo);

        List<AcaoTurno> acoesJogador = jogador.getAcoesCombate();
        List<AcaoTurno> acoesInimigo = inimigo.getAcoesCombate();

        gerenciadorLider.forcarModosPresos(campo);
        aplicarModos(campo.getSlotsJogador(), acoesJogador, true);
        aplicarModos(campo.getSlotsInimigo(), acoesInimigo, false);

        campo.setFaseAtual(FaseEnum.FARM);
        gerenciadorAura.aplicarAuraBase(campo);
        log("+3 Aura base para ambos.");

        if (campo.getLiderJogador().isMaoEspecialAtivo()) {
            gerenciadorAura.aplicarFarmMaoTudoAtacaEFarma(campo);
            log("Especial Mao: monstros do Jogador farmam e atacam neste turno.");
        } else {
            gerenciadorAura.resolverFarmJogador(campo);
        }
        if (campo.getLiderInimigo().isMaoEspecialAtivo()) {
            gerenciadorAura.aplicarFarmMaoTudoAtacaEFarma(campo, false);
            log("Especial Mao: monstros do Inimigo farmam e atacam neste turno.");
        } else {
            gerenciadorAura.resolverFarmInimigo(campo);
        }
        gerenciadorLider.aplicarPassivaGulag(campo, true);
        gerenciadorLider.aplicarPassivaGulag(campo, false);

        gerenciadorMagia.resolverContinuos(campo, logTurno);
        processarKO();
        if (campo.isJogoEncerrado()) return logTurno;

        campo.setFaseAtual(FaseEnum.ATAQUE);
        if (jogadorPrimeiro) {
            aplicarBonusLiderEAtacar(acoesJogador, true, true);
            if (campo.isJogoEncerrado()) return logTurno;
            aplicarBonusLiderEAtacar(acoesInimigo, false, true);
        } else {
            aplicarBonusLiderEAtacar(acoesInimigo, false, true);
            if (campo.isJogoEncerrado()) return logTurno;
            aplicarBonusLiderEAtacar(acoesJogador, true, true);
        }

        processarKO();
        if (campo.isJogoEncerrado()) return logTurno;

        campo.setFaseAtual(FaseEnum.STATUS);
        gerenciadorStatus.resolverTodosMonstros(campo.getSlotsJogador());
        gerenciadorStatus.resolverTodosMonstros(campo.getSlotsInimigo());
        processarKO();
        if (campo.isJogoEncerrado()) return logTurno;

        campo.setFaseAtual(FaseEnum.FIM_TURNO);
        gerenciadorLider.decrementarModosPresos(campo);
        campo.getLiderJogador().limparFlagsEspeciais();
        campo.getLiderInimigo().limparFlagsEspeciais();

        campo.setTurnoAtual(campo.getTurnoAtual() + 1);
        comprarParaProximoTurno();
        if (campo.isJogoEncerrado()) return logTurno;
        campo.setFaseAtual(FaseEnum.ATRIBUICAO);
        log("Turno " + campo.getTurnoAtual() + " iniciado.");
        return logTurno;
    }

    private void processarAtribuicaoPvp(boolean jogadorPrimeiro, TurnoJogador jogador, TurnoJogador inimigo) {
        if (jogadorPrimeiro) {
            processarAtribuicaoLado(jogador, true);
            processarAtribuicaoLado(inimigo, false);
        } else {
            processarAtribuicaoLado(inimigo, false);
            processarAtribuicaoLado(jogador, true);
        }
    }

    private void processarAtribuicaoLado(TurnoJogador turno, boolean jogador) {
        processarInvocacao(turno.getInvocacaoMonstro(), jogador);
        ajustarIndiceEfeitoAposInvocacao(turno);
        processarEfeito(turno.getAcaoEfeito(), jogador);
    }

    private void ajustarIndiceEfeitoAposInvocacao(TurnoJogador turno) {
        if (turno.getInvocacaoMonstro() == null || turno.getAcaoEfeito() == null) return;
        int indiceInvocacao = turno.getInvocacaoMonstro().getIndiceMao();
        int indiceEfeito = turno.getAcaoEfeito().getIndiceMao();
        if (indiceEfeito == indiceInvocacao) {
            throw new RegraInvalidaException("A mesma carta nao pode ser invocada e usada como efeito.");
        }
        if (indiceEfeito > indiceInvocacao) {
            turno.getAcaoEfeito().setIndiceMao(indiceEfeito - 1);
        }
    }

    private void processarInvocacao(InvocacaoTurno invocacao, boolean jogador) {
        if (invocacao == null) return;
        int slot = validarSlot(invocacao.getSlotDestino(), "slot de invocacao");
        MonstroInstancia[] slots = jogador ? campo.getSlotsJogador() : campo.getSlotsInimigo();
        if (slots[slot] != null) throw new RegraInvalidaException("Slot de monstro ocupado.");

        Carta carta = removerDaMao(jogador, invocacao.getIndiceMao(), CardType.MONSTRO);
        MonstroInstancia invocado = MonstroInstancia.fromCarta(carta);
        slots[slot] = invocado;
        log(rotulo(jogador) + " invocou " + invocado.getNome() + " no slot " + (slot + 1) + ".");
        gerenciadorArmadilha.aoInvocar(campo, !jogador, invocado, logTurno);
    }

    private void processarInvocacaoIA() {
        int slot = campo.getSlotVazioIndex(false);
        if (slot < 0) return;
        List<Carta> mao = campo.getMaoInimigo();
        for (int i = 0; i < mao.size(); i++) {
            Carta carta = mao.get(i);
            if (carta.isMonstro()) {
                mao.remove(i);
                MonstroInstancia invocado = MonstroInstancia.fromCarta(carta);
                campo.getSlotsInimigo()[slot] = invocado;
                log("Inimigo invocou " + invocado.getNome() + ".");
                gerenciadorArmadilha.aoInvocar(campo, true, invocado, logTurno);
                return;
            }
        }
    }

    private void processarEfeito(AcaoEfeitoTurno acao, boolean jogador) {
        if (acao == null) return;
        Carta carta = getCartaMao(jogador, acao.getIndiceMao());

        if (carta.isMagia()) {
            gerenciadorMagia.validarUso(campo, carta, acao, jogador);
            if (!gerenciadorAura.gastarAura(campo, jogador, carta.getCustoAura())) {
                throw new RegraInvalidaException("Aura insuficiente para usar " + carta.getNome() + ".");
            }
            carta = removerDaMao(jogador, acao.getIndiceMao(), CardType.MAGIA);
            if (gerenciadorArmadilha.aoJogarMagia(campo, !jogador, carta, logTurno)) {
                campo.getDescarte(jogador).add(carta);
                return;
            }
            gerenciadorMagia.jogar(campo, carta, acao, jogador, logTurno);
            return;
        }

        if (carta.isArmadilha()) {
            carta = removerDaMao(jogador, acao.getIndiceMao(), CardType.ARMADILHA);
            gerenciadorArmadilha.setar(campo, carta, jogador, logTurno);
            return;
        }

        if (carta.isEvolucao()) {
            if (jogador && jogadorEvoluiuNoTurno) throw new RegraInvalidaException("Apenas 1 evolucao por turno.");
            if (!jogador && inimigoEvoluiuNoTurno) throw new RegraInvalidaException("Apenas 1 evolucao por turno.");
            carta = removerDaMao(jogador, acao.getIndiceMao(), CardType.EVOLUCAO);
            gerenciadorEvolucao.evoluir(campo, carta, validarSlot(acao.getSlotMonstroAlvo(), "alvo de evolucao"),
                    jogador, logTurno);
            campo.getDescarte(jogador).add(carta);
            if (jogador) jogadorEvoluiuNoTurno = true;
            else inimigoEvoluiuNoTurno = true;
            return;
        }

        throw new RegraInvalidaException("Carta escolhida nao e efeito.");
    }

    private void processarEfeitoIA() {
        List<Carta> mao = campo.getMaoInimigo();
        if (!campo.temArmadilhaAtiva(false)) {
            for (int i = 0; i < mao.size(); i++) {
                Carta carta = mao.get(i);
                if (carta.isArmadilha()) {
                    mao.remove(i);
                    gerenciadorArmadilha.setar(campo, carta, false, logTurno);
                    return;
                }
            }
        }

        for (int i = 0; i < mao.size(); i++) {
            Carta carta = mao.get(i);
            if (carta.isMagia() && !campo.temMagiaAtiva(false)) {
                AcaoEfeitoTurno acaoIA = acaoPadraoIA();
                try {
                    gerenciadorMagia.validarUso(campo, carta, acaoIA, false);
                } catch (RegraInvalidaException ex) {
                    continue;
                }
                mao.remove(i);
                if (gerenciadorArmadilha.aoJogarMagia(campo, true, carta, logTurno)) {
                    campo.getDescarteInimigo().add(carta);
                    return;
                }
                gerenciadorMagia.jogar(campo, carta, acaoIA, false, logTurno);
                return;
            }
        }
    }

    private void processarEvolucaoIA() {
        if (inimigoEvoluiuNoTurno) return;
        List<Carta> mao = campo.getMaoInimigo();
        for (int i = 0; i < mao.size(); i++) {
            Carta carta = mao.get(i);
            if (!carta.isEvolucao()) continue;
            for (int slot = 0; slot < 3; slot++) {
                MonstroInstancia monstro = campo.getSlotsInimigo()[slot];
                if (monstro != null && monstro.getId().equals(carta.getBaseMonsterId())) {
                    mao.remove(i);
                    gerenciadorEvolucao.evoluir(campo, carta, slot, false, logTurno);
                    campo.getDescarteInimigo().add(carta);
                    inimigoEvoluiuNoTurno = true;
                    return;
                }
            }
        }
    }

    private AcaoEfeitoTurno acaoPadraoIA() {
        int aliado = primeiroSlotOcupado(false);
        int inimigo = primeiroSlotOcupado(true);
        return AcaoEfeitoTurno.builder()
                .slotMonstroAlvo(Math.max(0, aliado))
                .slotAlvo(Math.max(0, inimigo))
                .alvoInimigo(true)
                .build();
    }

    private void aplicarModos(MonstroInstancia[] slots, List<AcaoTurno> acoes, boolean jogador) {
        for (AcaoTurno acao : acoes) {
            int slot = validarSlot(acao.getSlotOrigem(), "slot de origem");
            MonstroInstancia m = slots[slot];
            if (m == null || m.estaPressoDef()) continue;
            if (acao.getModo() == null) throw new RegraInvalidaException("Modo da acao e obrigatorio.");
            if (m.temStatus(StatusEnum.CONGELADO) && acao.getModo() == ModoAcao.FARM) {
                m.setModoAtual(ModoAcao.DEFESA);
                log(m.getNome() + " esta Congelado e nao pode Farmar.");
                continue;
            }
            m.setModoAtual(acao.getModo());
        }
    }

    private void aplicarBonusLiderEAtacar(List<AcaoTurno> acoes, boolean jogador, boolean estrito) {
        Lider lider = jogador ? campo.getLiderJogador() : campo.getLiderInimigo();
        MonstroInstancia[] slots = jogador ? campo.getSlotsJogador() : campo.getSlotsInimigo();
        boolean napoleonBonus = lider.getTipo() == LiderEnum.NAPOLEON
                && gerenciadorLider.verificarNapoleonBonus(campo, jogador);
        if (napoleonBonus) {
            for (MonstroInstancia m : slots) if (m != null) m.setAtkBuff(m.getAtkBuff() + 15);
            log("Formacao Ofensiva de Napoleao: monstros de " + rotulo(jogador) + " recebem +15 ATK.");
        }
        if (lider.isGenghisAtacarTodos()) {
            resolverAtaquesGenghis(acoes, jogador, estrito);
        } else {
            resolverAtaques(acoes, jogador, lider.isNapoleonAtacarDuasVezes(), estrito);
        }
        if (napoleonBonus) {
            for (MonstroInstancia m : slots) if (m != null) m.setAtkBuff(m.getAtkBuff() - 15);
        }
    }

    private void resolverAtaques(List<AcaoTurno> acoes, boolean jogador, boolean duasVezes, boolean estrito) {
        MonstroInstancia[] slots = jogador ? campo.getSlotsJogador() : campo.getSlotsInimigo();
        Lider lider = jogador ? campo.getLiderJogador() : campo.getLiderInimigo();
        for (AcaoTurno acao : acoes) {
            if (!acao.isAtaque()) continue;
            MonstroInstancia m = slots[validarSlot(acao.getSlotOrigem(), "atacante")];
            if (m != null && !lider.isMaoEspecialAtivo() && m.getModoAtual() != ModoAcao.ATAQUE) continue;
            executarAtaque(acao, jogador, estrito);
            if (campo.isJogoEncerrado()) return;
            if (duasVezes) {
                log("Napoleao: ataque duplo.");
                executarAtaque(acao, jogador, estrito);
                if (campo.isJogoEncerrado()) return;
            }
        }
    }

    private void resolverAtaquesIA(List<AcaoTurno> acoes) {
        for (AcaoTurno acao : acoes) {
            if (!acao.isAtaque()) continue;
            executarAtaque(acao, false);
            if (campo.isJogoEncerrado()) return;
        }
    }

    private void executarAtaque(AcaoTurno acao, boolean jogadorAtaca) {
        executarAtaque(acao, jogadorAtaca, jogadorAtaca);
    }

    private void executarAtaque(AcaoTurno acao, boolean jogadorAtaca, boolean estrito) {
        MonstroInstancia[] aliados = jogadorAtaca ? campo.getSlotsJogador() : campo.getSlotsInimigo();
        MonstroInstancia[] inimigos = jogadorAtaca ? campo.getSlotsInimigo() : campo.getSlotsJogador();
        Lider liderAtacante = jogadorAtaca ? campo.getLiderJogador() : campo.getLiderInimigo();
        Lider liderDefensor = jogadorAtaca ? campo.getLiderInimigo() : campo.getLiderJogador();
        int slotOrigem = validarSlot(acao.getSlotOrigem(), "atacante");
        MonstroInstancia atacante = aliados[slotOrigem];
        if (atacante == null) {
            if (estrito) throw new RegraInvalidaException("Atacante vazio.");
            return;
        }
        if (atacante.isCancelarProximoAtaque()) {
            atacante.setCancelarProximoAtaque(false);
            log(atacante.getNome() + " teve o ataque cancelado.");
            return;
        }

        int indiceAtaque = escolherIndiceAtaque(atacante, acao.getIndiceAtaque());
        Ataque ataque = atacante.getTemplate().getAtaque(indiceAtaque);

        if (acao.isAlvoDiretoLider()) {
            if (campo.getMonstrosVivos(!jogadorAtaca) > 0) {
                if (estrito) throw new RegraInvalidaException("Ataque direto so e permitido sem monstros inimigos.");
                return;
            }
            if (!gerenciadorAura.gastarAura(campo, jogadorAtaca, ataque.getCustoAura())) {
                if (estrito) log("Aura insuficiente para " + atacante.getNome() + " usar " + ataque.getNome() + ".");
                return;
            }
            if (!ignoraArmadilhas(atacante, indiceAtaque)) {
                GerenciadorArmadilha.ResultadoArmadilha trap = gerenciadorArmadilha.aoAtaque(campo, !jogadorAtaca,
                        atacante, false, logTurno);
                if (trap.cancelarAtaque()) return;
            }
            liderDefensor.perderHp(8);
            log(atacante.getNome() + " atacou o Lider " + (jogadorAtaca ? "inimigo" : "jogador") + ". -8 HP.");
            verificarLideres();
            return;
        }

        int slotAlvo = validarSlot(acao.getSlotAlvo(), "alvo");
        MonstroInstancia defensor = inimigos[slotAlvo];
        if (defensor == null) {
            if (estrito) throw new RegraInvalidaException("Alvo vazio.");
            return;
        }
        if (!gerenciadorAura.gastarAura(campo, jogadorAtaca, ataque.getCustoAura())) {
            if (estrito) log("Aura insuficiente para " + atacante.getNome() + " usar " + ataque.getNome() + ".");
            return;
        }

        boolean vantagemTipo = atacante.getTipo().getMultiplicador(defensor.getTipo()) > 1.0;
        boolean neutralizarTipo = false;
        if (!ignoraArmadilhas(atacante, indiceAtaque)) {
            GerenciadorArmadilha.ResultadoArmadilha trap = gerenciadorArmadilha.aoAtaque(campo, !jogadorAtaca,
                    atacante, vantagemTipo, logTurno);
            if (trap.cancelarAtaque()) return;
            neutralizarTipo = trap.neutralizarTipo();
        }

        ResultadoChoque resultado = gerenciadorChoque.resolver(atacante, ataque, defensor,
                liderAtacante, liderDefensor, neutralizarTipo, ignoraDefesa(atacante, indiceAtaque, jogadorAtaca));
        resultado.setAtacanteJogador(jogadorAtaca);
        gerenciadorAura.aplicarAbsorcaoChoque(campo, resultado, jogadorAtaca);
        gerenciadorAura.aplicarBonusBloqueio(campo, resultado, !jogadorAtaca);
        aplicarEfeitosEspeciaisAtaque(atacante, defensor, ataque, indiceAtaque, resultado, jogadorAtaca);
        if (resultado.foiVitoria()) {
            resolvedorDeclarativo.aplicar(campo, atacante.getTemplate(), EfeitoTrigger.AO_VENCER_CHOQUE,
                    jogadorAtaca, null, atacante, defensor, null, logTurno);
        }
        log(resultado.getDescricaoLog());
        verificarLideres();
    }

    private int escolherIndiceAtaque(MonstroInstancia atacante, int solicitado) {
        int indice = solicitado;
        if (atacante.temStatus(StatusEnum.CONFUSO)) {
            indice = ThreadLocalRandom.current().nextInt(atacante.getTemplate().getAtaques().size());
            log(atacante.getNome() + " esta Confuso e escolheu ataque aleatorio.");
        }
        if (atacante.temStatus(StatusEnum.CONGELADO) && indice >= 2) {
            indice = 0;
            log(atacante.getNome() + " esta Congelado e nao pode usar o Ataque 3.");
        }
        return Math.max(0, Math.min(indice, atacante.getTemplate().getAtaques().size() - 1));
    }

    private boolean ignoraDefesa(MonstroInstancia atacante, int indiceAtaque, boolean jogadorAtaca) {
        Carta magia = campo.getMagiaAtiva(jogadorAtaca);
        return "wargreymon".equals(atacante.getId())
                || "magonegro".equals(atacante.getId())
                || ("agumon".equals(atacante.getId()) && indiceAtaque == 2)
                || (magia != null && "portal_sombras".equals(magia.getId())
                && atacante.getTipo() == com.team.multiversaltcg.game.enums.TipoUniversal.SOMBRA);
    }

    private boolean ignoraArmadilhas(MonstroInstancia atacante, int indiceAtaque) {
        return "dragao".equals(atacante.getId()) && indiceAtaque == 2;
    }

    private void aplicarEfeitosEspeciaisAtaque(MonstroInstancia atacante, MonstroInstancia defensor,
                                               Ataque ataque, int indiceAtaque, ResultadoChoque resultado,
                                               boolean jogadorAtaca) {
        if (!resultado.foiVitoria()) return;
        MonstroInstancia[] aliados = jogadorAtaca ? campo.getSlotsJogador() : campo.getSlotsInimigo();
        MonstroInstancia[] inimigos = jogadorAtaca ? campo.getSlotsInimigo() : campo.getSlotsJogador();

        switch (atacante.getId()) {
            case "charizard" -> {
                if (indiceAtaque == 2) {
                    for (MonstroInstancia m : inimigos) if (m != null) gerenciadorStatus.aplicarStatus(m, StatusEnum.QUEIMADO, 2);
                }
            }
            case "pikachu" -> {
                if (indiceAtaque == 1) pressionarOutroInimigo(inimigos, defensor, 1);
            }
            case "squirtle" -> {
                if (indiceAtaque == 0) campo.adicionarAura(jogadorAtaca, 1);
                if (indiceAtaque == 2) defensor.setDefBuff(defensor.getDefBuff() - 20);
            }
            case "bulbasaur" -> {
                if (indiceAtaque == 0) curarMaisPressionado(aliados, 1);
            }
            case "garurumon" -> {
                if (indiceAtaque == 2) gerenciadorStatus.aplicarStatus(defensor, StatusEnum.ENVENENADO, 4);
            }
            case "dragao" -> {
                if (indiceAtaque == 1) defensor.setCancelarProximoAtaque(true);
            }
            case "zagueiro" -> {
                if (indiceAtaque == 1) {
                    atacante.setDefBuff(atacante.getDefBuff() + 30);
                    for (MonstroInstancia m : aliados) if (m != null && m != atacante) m.setDefBuff(m.getDefBuff() + 20);
                }
            }
            case "orei" -> {
                if (indiceAtaque == 2) for (MonstroInstancia m : aliados) if (m != null) m.curarPressao(1);
            }
            case "patamon" -> {
                if (indiceAtaque == 0) gerenciadorCompra.comprar(campo, jogadorAtaca, logTurno, true);
                if (indiceAtaque == 1) curarMaisPressionado(aliados, 2);
            }
            case "angemon" -> curarMaisPressionado(aliados, 1);
            default -> {
            }
        }
    }

    private void resolverAtaquesGenghis(List<AcaoTurno> acoes, boolean jogador, boolean estrito) {
        log("Horda Mongol: cada monstro ataca todos os inimigos.");
        for (AcaoTurno acao : acoes) {
            if (!acao.isAtaque()) continue;
            MonstroInstancia[] inimigos = jogador ? campo.getSlotsInimigo() : campo.getSlotsJogador();
            for (int i = 0; i < 3; i++) {
                MonstroInstancia defensor = inimigos[i];
                if (defensor == null) continue;
                AcaoTurno horda = AcaoTurno.builder()
                        .slotOrigem(acao.getSlotOrigem())
                        .modo(ModoAcao.ATAQUE)
                        .indiceAtaque(acao.getIndiceAtaque())
                        .slotAlvo(i)
                        .build();
                executarAtaque(horda, jogador, estrito);
                if (campo.isJogoEncerrado()) return;
            }
        }
    }

    private void pressionarOutroInimigo(MonstroInstancia[] inimigos, MonstroInstancia defensor, int quantidade) {
        for (MonstroInstancia m : inimigos) {
            if (m != null && m != defensor) {
                m.adicionarPressao(quantidade);
                return;
            }
        }
    }

    private void curarMaisPressionado(MonstroInstancia[] slots, int quantidade) {
        MonstroInstancia alvo = null;
        for (MonstroInstancia m : slots) {
            if (m != null && (alvo == null || m.getPressure() > alvo.getPressure())) alvo = m;
        }
        if (alvo != null) alvo.curarPressao(quantidade);
    }

    private void processarKO() {
        gerenciadorArmadilha.antesDoKO(campo, logTurno);
        logTurno.addAll(gerenciadorKO.verificarEProcessar(campo));
        if (campo.isJogoEncerrado()) log("Fim de jogo. Vencedor: " + campo.getVencedor() + ".");
    }

    private void verificarLideres() {
        if (campo.getLiderJogador().isDerrotado()) campo.encerrarJogo("INIMIGO");
        else if (campo.getLiderInimigo().isDerrotado()) campo.encerrarJogo("JOGADOR");
    }

    private void comprarParaProximoTurno() {
        gerenciadorCompra.comprarTurno(campo, true, logTurno);
        if (campo.isJogoEncerrado()) return;
        gerenciadorCompra.comprarTurno(campo, false, logTurno);
    }

    private Carta removerDaMao(boolean jogador, int indiceMao, CardType tipoEsperado) {
        Carta carta = getCartaMao(jogador, indiceMao);
        if (carta.getCardType() != tipoEsperado) {
            throw new RegraInvalidaException("Carta selecionada nao e do tipo " + tipoEsperado + ".");
        }
        campo.getMao(jogador).remove(indiceMao);
        return carta;
    }

    private Carta getCartaMao(boolean jogador, int indiceMao) {
        List<Carta> mao = campo.getMao(jogador);
        if (indiceMao < 0 || indiceMao >= mao.size()) throw new RegraInvalidaException("Indice de mao invalido: " + indiceMao);
        return mao.get(indiceMao);
    }

    private int primeiroSlotOcupado(boolean jogador) {
        MonstroInstancia[] slots = jogador ? campo.getSlotsJogador() : campo.getSlotsInimigo();
        for (int i = 0; i < slots.length; i++) if (slots[i] != null) return i;
        return -1;
    }

    private int validarSlot(int slot, String campoNome) {
        if (slot < 0 || slot > 2) throw new RegraInvalidaException("Indice invalido para " + campoNome + ": " + slot);
        return slot;
    }

    public void ativarEspecial(boolean jogador) {
        Lider lider = jogador ? campo.getLiderJogador() : campo.getLiderInimigo();
        if (!lider.podeUsarEspecial()) {
            log("Especial de " + lider.getTipo().getNome() + " ja foi usado.");
            return;
        }
        gerenciadorLider.ativarEspecial(campo, jogador);
        log("Especial de " + lider.getTipo().getNome() + " ativado.");
    }

    public CampoBatalha getCampo() {
        return campo;
    }

    public List<String> getLogTurno() {
        return logTurno;
    }

    private void log(String mensagem) {
        logTurno.add(mensagem);
    }

    private String rotulo(boolean jogador) {
        return jogador ? "Jogador" : "Inimigo";
    }
}
