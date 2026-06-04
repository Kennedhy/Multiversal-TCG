package com.team.multiversaltcg.game.engine;

import com.team.multiversaltcg.game.enums.TipoEfeito;
import com.team.multiversaltcg.game.enums.TipoUniversal;
import com.team.multiversaltcg.game.enums.EfeitoTrigger;
import com.team.multiversaltcg.game.model.AcaoEfeitoTurno;
import com.team.multiversaltcg.game.model.CampoBatalha;
import com.team.multiversaltcg.game.model.Carta;
import com.team.multiversaltcg.game.model.Lider;
import com.team.multiversaltcg.game.model.MonstroInstancia;
import com.team.multiversaltcg.game.model.RegraInvalidaException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GerenciadorMagia {

    private final GerenciadorCompra gerenciadorCompra;
    private final GerenciadorStatus gerenciadorStatus;
    private final ResolvedorEfeitoDeclarativo resolvedorDeclarativo;

    public GerenciadorMagia(GerenciadorCompra gerenciadorCompra,
                            GerenciadorStatus gerenciadorStatus,
                            ResolvedorEfeitoDeclarativo resolvedorDeclarativo) {
        this.gerenciadorCompra = gerenciadorCompra;
        this.gerenciadorStatus = gerenciadorStatus;
        this.resolvedorDeclarativo = resolvedorDeclarativo;
    }

    public void jogar(CampoBatalha campo, Carta carta, AcaoEfeitoTurno acao,
                      boolean jogador, List<String> log) {
        validarAlvo(campo, carta, acao, jogador);
        if (carta.getDuracao() == 0) {
            aplicarEfeitoAoJogar(campo, carta, acao, jogador, log);
            campo.getDescarte(jogador).add(carta);
            log.add((jogador ? "Jogador" : "Inimigo") + " usou magia: " + carta.getNome() + ".");
            return;
        }

        Carta anterior = campo.getMagiaAtiva(jogador);
        if (anterior != null) {
            campo.getDescarte(jogador).add(anterior);
            log.add("Magia anterior de " + (jogador ? "Jogador" : "Inimigo") + " foi descartada.");
        }

        Carta ativa = carta.copy();
        ativa.setTurnosRestantes(ativa.getDuracao());
        campo.setMagiaAtiva(jogador, ativa);
        aplicarEfeitoAoJogar(campo, ativa, acao, jogador, log);
        log.add((jogador ? "Jogador" : "Inimigo") + " ativou magia: " + ativa.getNome() + ".");
    }

    public void resolverContinuos(CampoBatalha campo, List<String> log) {
        resolverContinuo(campo, true, log);
        resolverContinuo(campo, false, log);
    }

    public boolean evolucaoGratis(CampoBatalha campo, boolean jogador) {
        return campo.getMagiaAtiva(jogador) != null
                && "nexo_digital".equals(campo.getMagiaAtiva(jogador).getId());
    }

    public void validarUso(CampoBatalha campo, Carta carta, AcaoEfeitoTurno acao, boolean jogador) {
        validarAlvo(campo, carta, acao, jogador);
    }

    private void aplicarEfeitoAoJogar(CampoBatalha campo, Carta carta, AcaoEfeitoTurno acao,
                                      boolean jogador, List<String> log) {
        if (carta.getEfeito() != null) {
            switch (carta.getEfeito()) {
                case AURA -> campo.adicionarAura(jogador, carta.getValor());
                case CURAR_LIDER -> {
                    Lider lider = jogador ? campo.getLiderJogador() : campo.getLiderInimigo();
                    lider.setHp(Math.min(lider.getHpMaximo(), lider.getHp() + carta.getValor()));
                }
                case BUFF_ATK, BUFF_DEF -> aplicarBuff(campo, carta, acao, jogador, log);
                case PRESSAO_ALVO -> aplicarPressao(campo, carta, acao, jogador, log);
                case CURA_STATUS -> {
                    MonstroInstancia alvo = getAlvoAliado(campo, jogador, acao.getSlotMonstroAlvo());
                    gerenciadorStatus.limparTodos(alvo);
                }
                case DRAW_CARTAS -> {
                    for (int i = 0; i < carta.getValor(); i++) {
                        gerenciadorCompra.comprar(campo, jogador, log, true);
                        if (campo.isJogoEncerrado()) return;
                    }
                }
                case IGNORAR_DEFESA -> getAlvo(campo, jogador, acao).setIgnorarDefBuffTurnos(1);
                case IMUNIDADE_STATUS -> {
                    MonstroInstancia alvo = getAlvoAliado(campo, jogador, acao.getSlotMonstroAlvo());
                    alvo.curarPressao(1);
                    alvo.setImune(true);
                    alvo.setImunoTurnos(Math.max(alvo.getImunoTurnos(), 1));
                }
                case BUSCA_DECK -> buscarNoTopo(campo, jogador, acao, log);
                default -> {
                }
            }
        }
        resolvedorDeclarativo.aplicar(campo, carta, EfeitoTrigger.AO_JOGAR, jogador, acao,
                null, null, null, log);
    }

    private void resolverContinuo(CampoBatalha campo, boolean jogador, List<String> log) {
        Carta magia = campo.getMagiaAtiva(jogador);
        if (magia == null) return;

        MonstroInstancia[] slots = jogador ? campo.getSlotsJogador() : campo.getSlotsInimigo();
        if (magia.getEfeito() != null) {
            switch (magia.getEfeito()) {
                case BOOST_ATK_TIPO -> {
                    for (MonstroInstancia m : slots) {
                        if (m != null && m.getTipo() == magia.getTipoAlvo()) {
                            m.setAtkBuff(m.getAtkBuff() + magia.getValor());
                        }
                    }
                }
                case CURA_STATUS -> curarMaisPressionado(slots, 1);
                case IMUNIDADE_STATUS -> {
                    for (MonstroInstancia m : slots) {
                        if (m != null && m.getTipo() == TipoUniversal.ETER) {
                            m.setImune(true);
                            m.setImunoTurnos(Math.max(m.getImunoTurnos(), 1));
                        }
                    }
                }
                default -> {
                }
            }
        }
        resolvedorDeclarativo.aplicar(campo, magia, EfeitoTrigger.CONTINUO_FIM_TURNO, jogador,
                null, null, null, null, log);

        if (magia.getDuracao() > 0) {
            magia.setTurnosRestantes(magia.getTurnosRestantes() - 1);
            if (magia.getTurnosRestantes() <= 0) {
                campo.getDescarte(jogador).add(magia);
                campo.setMagiaAtiva(jogador, null);
                log.add("Magia expirou: " + magia.getNome() + ".");
            }
        }
    }

    private void validarAlvo(CampoBatalha campo, Carta carta, AcaoEfeitoTurno acao, boolean jogador) {
        boolean precisaAlvo = carta.getEfeito() != null && switch (carta.getEfeito()) {
            case BUFF_ATK, BUFF_DEF, CURA_STATUS, IMUNIDADE_STATUS, PRESSAO_ALVO, IGNORAR_DEFESA -> true;
            default -> false;
        };
        String modoCustom = resolvedorDeclarativo.targetingModeAoJogar(carta);
        precisaAlvo = precisaAlvo || !"NONE".equals(modoCustom);
        if (precisaAlvo && acao == null) {
            throw new RegraInvalidaException("Alvo do efeito e obrigatorio.");
        }
        if (carta.getEfeito() != null) {
            switch (carta.getEfeito()) {
                case BUFF_ATK, BUFF_DEF, CURA_STATUS, IMUNIDADE_STATUS -> getAlvoAliado(campo, jogador, acao.getSlotMonstroAlvo());
                case PRESSAO_ALVO, IGNORAR_DEFESA -> getAlvo(campo, jogador, acao);
                default -> {
                }
            }
        }
        if ("OWN".equals(modoCustom)) getAlvoAliado(campo, jogador, acao.getSlotMonstroAlvo());
        if ("ENEMY".equals(modoCustom)) getAlvo(campo, jogador, acao);
    }

    private void aplicarBuff(CampoBatalha campo, Carta carta, AcaoEfeitoTurno acao,
                             boolean jogador, List<String> log) {
        MonstroInstancia alvo = getAlvoAliado(campo, jogador, acao.getSlotMonstroAlvo());
        if (carta.getEfeito() == TipoEfeito.BUFF_ATK) {
            alvo.setAtkBuff(alvo.getAtkBuff() + carta.getValor());
        } else {
            alvo.setDefBuff(alvo.getDefBuff() + carta.getValor());
        }
        log.add(carta.getNome() + " fortaleceu " + alvo.getNome() + ".");
    }

    private void aplicarPressao(CampoBatalha campo, Carta carta, AcaoEfeitoTurno acao,
                                boolean jogador, List<String> log) {
        MonstroInstancia alvo = getAlvo(campo, jogador, acao);
        alvo.adicionarPressao(carta.getValor());
        log.add(carta.getNome() + " aplicou " + carta.getValor() + " Pressao em " + alvo.getNome() + ".");
    }

    private void buscarNoTopo(CampoBatalha campo, boolean jogador, AcaoEfeitoTurno acao, List<String> log) {
        List<Carta> deck = campo.getDeck(jogador);
        if (deck.isEmpty()) return;
        int ver = Math.min(3, deck.size());
        int escolha = Math.max(0, Math.min(acao.getEscolhaBusca(), ver - 1));
        List<Carta> topo = new ArrayList<>(deck.subList(0, ver));
        deck.subList(0, ver).clear();
        Carta escolhida = topo.remove(escolha);
        campo.getMao(jogador).add(escolhida);
        deck.addAll(topo);
        Collections.shuffle(deck);
        log.add((jogador ? "Jogador" : "Inimigo") + " buscou uma carta com Nexo Digital.");
    }

    private MonstroInstancia getAlvo(CampoBatalha campo, boolean jogador, AcaoEfeitoTurno acao) {
        if (acao == null) throw new RegraInvalidaException("Alvo do efeito e obrigatorio.");
        MonstroInstancia[] slots = acao.isAlvoInimigo()
                ? (jogador ? campo.getSlotsInimigo() : campo.getSlotsJogador())
                : (jogador ? campo.getSlotsJogador() : campo.getSlotsInimigo());
        int slot = validarSlot(acao.getSlotAlvo(), "alvo de efeito");
        if (slots[slot] == null) throw new RegraInvalidaException("Alvo de efeito vazio.");
        return slots[slot];
    }

    private MonstroInstancia getAlvoAliado(CampoBatalha campo, boolean jogador, int slotAlvo) {
        MonstroInstancia[] slots = jogador ? campo.getSlotsJogador() : campo.getSlotsInimigo();
        int slot = validarSlot(slotAlvo, "alvo aliado");
        if (slots[slot] == null) throw new RegraInvalidaException("Alvo aliado vazio.");
        return slots[slot];
    }

    private void curarMaisPressionado(MonstroInstancia[] slots, int quantidade) {
        MonstroInstancia alvo = null;
        for (MonstroInstancia m : slots) {
            if (m != null && (alvo == null || m.getPressure() > alvo.getPressure())) {
                alvo = m;
            }
        }
        if (alvo != null) alvo.curarPressao(quantidade);
    }

    private int validarSlot(int slot, String campoNome) {
        if (slot < 0 || slot > 2) {
            throw new RegraInvalidaException("Indice invalido para " + campoNome + ": " + slot);
        }
        return slot;
    }
}
