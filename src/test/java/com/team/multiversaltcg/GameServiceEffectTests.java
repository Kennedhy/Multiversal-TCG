package com.team.multiversaltcg;

import com.team.multiversaltcg.game.enums.CardType;
import com.team.multiversaltcg.game.enums.EfeitoAcaoTipo;
import com.team.multiversaltcg.game.enums.EfeitoAlvo;
import com.team.multiversaltcg.game.enums.EfeitoTrigger;
import com.team.multiversaltcg.game.enums.ModoAcao;
import com.team.multiversaltcg.game.enums.StatusEnum;
import com.team.multiversaltcg.game.engine.GerenciadorEvolucao;
import com.team.multiversaltcg.game.engine.GerenciadorMagia;
import com.team.multiversaltcg.game.engine.GerenciadorCompra;
import com.team.multiversaltcg.game.engine.GerenciadorStatus;
import com.team.multiversaltcg.game.model.AcaoTurno;
import com.team.multiversaltcg.game.model.AcaoEfeitoTurno;
import com.team.multiversaltcg.game.model.CampoBatalha;
import com.team.multiversaltcg.game.model.Carta;
import com.team.multiversaltcg.game.model.EfeitoAcaoDeclarativa;
import com.team.multiversaltcg.game.model.EfeitoRegraDeclarativa;
import com.team.multiversaltcg.game.model.InvocacaoTurno;
import com.team.multiversaltcg.game.model.MonstroInstancia;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.model.TurnoJogador;
import com.team.multiversaltcg.game.service.CartaDataService;
import com.team.multiversaltcg.game.service.GameService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameServiceEffectTests {

    private final CartaDataService cartas = new CartaDataService();

    @Test
    void magiaGeraAuraEVaiParaDescarte() {
        GameService service = novoJogo();
        CampoBatalha campo = service.getCampo();
        campo.getMaoJogador().clear();
        campo.getMaoJogador().add(cartas.getById("fonte_aura"));

        service.processarTurno(TurnoJogador.builder()
                .acaoEfeito(AcaoEfeitoTurno.builder().indiceMao(0).build())
                .acoesCombate(List.of())
                .build());

        assertThat(campo.getAuraJogador()).isGreaterThanOrEqualTo(7);
        assertThat(campo.getDescarteJogador()).extracting("id").contains("fonte_aura");
    }

    @Test
    void armadilhaDisparaQuandoInimigoInvoca() {
        GameService service = novoJogo();
        CampoBatalha campo = service.getCampo();
        limparCampo(campo);
        campo.getMaoJogador().clear();
        campo.getMaoJogador().add(cartas.getById("buraco_instavel"));
        campo.getMaoInimigo().clear();
        campo.getMaoInimigo().add(cartas.getById("charizard"));

        service.processarTurno(TurnoJogador.builder()
                .acaoEfeito(AcaoEfeitoTurno.builder()
                        .indiceMao(0)
                        .slotZona(0)
                        .build())
                .acoesCombate(List.of())
                .build());

        assertThat(campo.getZonasEfeitoJogador()[0]).isNull();
        assertThat(campo.getSlotsInimigo()[0]).isNotNull();
        assertThat(campo.getSlotsInimigo()[0].getPressure()).isEqualTo(1);
        assertThat(campo.getDescarteJogador()).extracting("id").contains("buraco_instavel");
    }

    @Test
    void evolucaoSubstituiMonstroCompativel() {
        GameService service = novoJogo();
        CampoBatalha campo = service.getCampo();
        limparCampo(campo);
        campo.getMaoJogador().clear();
        campo.getMaoJogador().add(cartas.getById("pikachu"));
        campo.getMaoJogador().add(cartas.getById("evoluir_raichu"));
        campo.getMaoInimigo().clear();

        service.processarTurno(TurnoJogador.builder()
                .invocacaoMonstro(InvocacaoTurno.builder()
                        .indiceMao(0)
                        .slotDestino(0)
                        .build())
                .acaoEfeito(AcaoEfeitoTurno.builder()
                        .indiceMao(1)
                        .slotMonstroAlvo(0)
                        .build())
                .acoesCombate(List.of())
                .build());

        assertThat(campo.getSlotsJogador()[0]).isNotNull();
        assertThat(campo.getSlotsJogador()[0].getId()).isEqualTo("raichu");
        assertThat(campo.getDescarteJogador()).extracting("id").contains("evoluir_raichu");
    }

    @Test
    void magiaComAlvoInvalidoNaoSaiDaMaoNemGastaAura() {
        GameService service = novoJogo();
        CampoBatalha campo = service.getCampo();
        limparCampo(campo);
        campo.getMaoJogador().clear();
        campo.getMaoJogador().add(cartas.getById("forja_atk"));
        campo.setAuraJogador(5);

        assertThatThrownBy(() -> service.processarTurno(TurnoJogador.builder()
                .acaoEfeito(AcaoEfeitoTurno.builder()
                        .indiceMao(0)
                        .slotMonstroAlvo(0)
                        .build())
                .acoesCombate(List.of())
                .build()))
                .isInstanceOf(RegraInvalidaException.class)
                .hasMessageContaining("Alvo aliado vazio");

        assertThat(campo.getMaoJogador()).extracting("id").containsExactly("forja_atk");
        assertThat(campo.getDescarteJogador()).isEmpty();
        assertThat(campo.getAuraJogador()).isEqualTo(5);
    }

    @Test
    void invocacaoEmSlotOcupadoNaoRemoveCartaDaMao() {
        GameService service = novoJogo();
        CampoBatalha campo = service.getCampo();
        limparCampo(campo);
        campo.getMaoJogador().clear();
        campo.getMaoJogador().add(cartas.getById("charizard"));
        campo.getSlotsJogador()[0] = MonstroInstancia.fromCarta(cartas.getById("pikachu"));

        assertThatThrownBy(() -> service.processarTurno(TurnoJogador.builder()
                .invocacaoMonstro(InvocacaoTurno.builder()
                        .indiceMao(0)
                        .slotDestino(0)
                        .build())
                .acoesCombate(List.of())
                .build()))
                .isInstanceOf(RegraInvalidaException.class)
                .hasMessageContaining("Slot de monstro ocupado");

        assertThat(campo.getMaoJogador()).extracting("id").containsExactly("charizard");
    }

    @Test
    void ataqueDiretoInvalidoNaoGastaAuraDoAtaque() {
        GameService service = novoJogo();
        CampoBatalha campo = service.getCampo();
        limparCampo(campo);
        campo.getMaoJogador().clear();
        campo.getMaoInimigo().clear();
        campo.getSlotsJogador()[0] = MonstroInstancia.fromCarta(cartas.getById("charizard"));
        campo.getSlotsInimigo()[0] = MonstroInstancia.fromCarta(cartas.getById("pikachu"));
        campo.setAuraJogador(10);

        assertThatThrownBy(() -> service.processarTurno(TurnoJogador.builder()
                .acoesCombate(List.of(AcaoTurno.builder()
                        .slotOrigem(0)
                        .modo(ModoAcao.ATAQUE)
                        .indiceAtaque(0)
                        .alvoDiretoLider(true)
                        .build()))
                .build()))
                .isInstanceOf(RegraInvalidaException.class)
                .hasMessageContaining("Ataque direto");

        assertThat(campo.getAuraJogador()).isEqualTo(13);
    }

    @Test
    void compraComDeckVazioEncerraJogo() {
        GameService service = novoJogo();
        CampoBatalha campo = service.getCampo();
        limparCampo(campo);
        campo.getDeckJogador().clear();
        campo.getMaoJogador().clear();
        campo.getMaoInimigo().clear();

        service.processarTurno(TurnoJogador.builder()
                .acoesCombate(List.of())
                .build());

        assertThat(campo.isJogoEncerrado()).isTrue();
        assertThat(campo.getVencedor()).isEqualTo("INIMIGO");
    }

    @Test
    void evolucaoPreservaPressaoStatusEBuffs() {
        GameService service = novoJogo();
        CampoBatalha campo = service.getCampo();
        limparCampo(campo);
        MonstroInstancia pikachu = MonstroInstancia.fromCarta(cartas.getById("pikachu"));
        pikachu.adicionarPressao(2);
        pikachu.setAtkBuff(7);
        pikachu.setDefBuff(5);
        pikachu.aplicarStatus(StatusEnum.CONFUSO, 2);
        campo.getSlotsJogador()[0] = pikachu;

        new GerenciadorEvolucao(cartas).evoluir(campo, cartas.getById("evoluir_raichu"), 0, true, new java.util.ArrayList<>());

        MonstroInstancia raichu = campo.getSlotsJogador()[0];
        assertThat(raichu.getId()).isEqualTo("raichu");
        assertThat(raichu.getPressure()).isEqualTo(2);
        assertThat(raichu.getAtkBuff()).isEqualTo(7);
        assertThat(raichu.getDefBuff()).isEqualTo(5);
        assertThat(raichu.temStatus(StatusEnum.CONFUSO)).isTrue();
    }

    @Test
    void magiaAtivaSubstituiAnteriorEExpira() {
        GameService service = novoJogo();
        CampoBatalha campo = service.getCampo();
        java.util.ArrayList<String> log = new java.util.ArrayList<>();
        GerenciadorMagia magia = new GerenciadorMagia(new GerenciadorCompra(), new GerenciadorStatus());

        magia.jogar(campo, cartas.getById("campo_sagrado"), AcaoEfeitoTurno.builder().build(), true, log);
        magia.jogar(campo, cartas.getById("nexo_digital"), AcaoEfeitoTurno.builder().escolhaBusca(0).build(), true, log);

        assertThat(campo.getMagiaAtivaJogador().getId()).isEqualTo("nexo_digital");
        assertThat(campo.getDescarteJogador()).extracting("id").contains("campo_sagrado");

        magia.resolverContinuos(campo, log);
        magia.resolverContinuos(campo, log);
        magia.resolverContinuos(campo, log);

        assertThat(campo.getMagiaAtivaJogador()).isNull();
        assertThat(campo.getDescarteJogador()).extracting("id").contains("nexo_digital");
    }

    @Test
    void magiaCustomDeclarativaAplicaPressaoNoAlvo() {
        GameService service = novoJogo();
        CampoBatalha campo = service.getCampo();
        limparCampo(campo);
        campo.getSlotsInimigo()[0] = MonstroInstancia.fromCarta(cartas.getById("pikachu"));
        Carta custom = Carta.builder()
                .id("magia_custom")
                .nome("Magia Custom")
                .cardType(CardType.MAGIA)
                .duracao(0)
                .regras(List.of(EfeitoRegraDeclarativa.builder()
                        .trigger(EfeitoTrigger.AO_JOGAR)
                        .target(EfeitoAlvo.ENEMY_TARGET)
                        .actions(List.of(EfeitoAcaoDeclarativa.builder()
                                .tipo(EfeitoAcaoTipo.PRESSAO)
                                .valor(2)
                                .build()))
                        .build()))
                .build();
        GerenciadorMagia magia = new GerenciadorMagia(new GerenciadorCompra(), new GerenciadorStatus());

        magia.jogar(campo, custom, AcaoEfeitoTurno.builder()
                .slotAlvo(0)
                .alvoInimigo(true)
                .build(), true, new java.util.ArrayList<>());

        assertThat(campo.getSlotsInimigo()[0].getPressure()).isEqualTo(2);
        assertThat(campo.getDescarteJogador()).extracting("id").contains("magia_custom");
    }

    private GameService novoJogo() {
        GameService service = new GameService(cartas);
        service.iniciarPartida("MAO");
        return service;
    }

    private void limparCampo(CampoBatalha campo) {
        for (int i = 0; i < 3; i++) {
            campo.getSlotsJogador()[i] = null;
            campo.getSlotsInimigo()[i] = null;
            campo.getZonasEfeitoJogador()[i] = null;
            campo.getZonasEfeitoInimigo()[i] = null;
        }
    }
}
