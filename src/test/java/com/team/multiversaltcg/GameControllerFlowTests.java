package com.team.multiversaltcg;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.team.multiversaltcg.game.service.PlayerCollectionService;
import com.team.multiversaltcg.modules.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "debug=false",
        "logging.level.root=WARN",
        "logging.level.org.springframework=WARN",
        "logging.level.org.hibernate.SQL=WARN"
})
@AutoConfigureMockMvc
class GameControllerFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerCollectionService playerCollectionService;

    @Test
    void authRegistroLoginEMeFuncionamComJwt() throws Exception {
        String username = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String token = registrarUsuario(username, "senha123");

        assertThat(token).isNotBlank();
        assertThat(userRepository.findByUsername(username).orElseThrow().getPassword()).startsWith("$2");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authJson(username, "senha123")))
                .andExpect(status().isBadRequest());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authJson(username, "senha123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(username))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String loginToken = objectMapper.readTree(loginResponse).get("token").asText();
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", bearer(loginToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));

        mockMvc.perform(get("/api/players/{playerId}/profile", username)
                        .header("Authorization", bearer(loginToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(username))
                .andExpect(jsonPath("$.coins").value(1000))
                .andExpect(jsonPath("$.initialBonusGranted").value(true));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginComSenhaErradaRetornaUnauthorized() throws Exception {
        String username = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        registrarUsuario(username, "senha123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authJson(username, "errada123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void decksDoJogadorExigemTokenDoDono() throws Exception {
        String username = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String outro = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String token = registrarUsuario(username, "senha123");

        mockMvc.perform(get("/api/players/{playerId}/decks", username))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/api/players/{playerId}/decks", outro)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/players/{playerId}/decks", username)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Deck Bloqueado",
                                  "cards": [
                                    { "id": "charizard", "deckCopies": 30 }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest());

        playerCollectionService.addCard(username, "charizard", 30);
        mockMvc.perform(post("/api/players/{playerId}/decks", username)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Deck Seguro",
                                  "cards": [
                                    { "id": "charizard", "deckCopies": 30 }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value(username))
                .andExpect(jsonPath("$.total").value(30));
    }

    @Test
    void lojaPacotesEColecaoFuncionamComJwt() throws Exception {
        String username = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String token = registrarUsuario(username, "senha123");

        mockMvc.perform(get("/api/players/{playerId}/shop", username)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coins").value(1000))
                .andExpect(jsonPath("$.packCost").value(100))
                .andExpect(jsonPath("$.cardsPerPack").value(5));

        mockMvc.perform(post("/api/players/{playerId}/packs/buy", username)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cards").isArray())
                .andExpect(jsonPath("$.cards.length()").value(5))
                .andExpect(jsonPath("$.coinsRemaining").value(900));

        mockMvc.perform(get("/api/players/{playerId}/collection", username)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOwned").value(5))
                .andExpect(jsonPath("$.uniqueOwned").value(org.hamcrest.Matchers.greaterThan(0)));

        mockMvc.perform(get("/api/players/{playerId}/packs/history", username)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cards.length()").value(5));
    }

    @Test
    void iniciarPartidaComDeckSalvoExigeJwtMasPadraoContinuaPublico() throws Exception {
        String username = "user_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String token = registrarUsuario(username, "senha123");
        playerCollectionService.addCard(username, "charizard", 30);
        String deckResponse = criarDeckCharizard(username, token, "Deck Protegido");
        String deckId = objectMapper.readTree(deckResponse).get("id").asText();

        mockMvc.perform(post("/api/game/iniciar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomId": "%s",
                                  "liderId": "MAO",
                                  "playerId": "outro",
                                  "deckId": "%s"
                                }
                                """.formatted("sem-token-" + UUID.randomUUID(), deckId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/game/iniciar")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomId": "%s",
                                  "liderId": "MAO",
                                  "playerId": "outro",
                                  "deckId": "%s"
                                }
                                """.formatted("com-token-" + UUID.randomUUID(), deckId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maoJogador[0].id").value("charizard"));

        iniciarSala("publico");
    }

    @Test
    void iniciarPartidaRetornaCampoVazioMaoInicialEImagens() throws Exception {
        JsonNode estado = iniciarSala("inicio");

        assertThat(estado.get("slotsJogador")).hasSize(3);
        assertThat(estado.get("slotsJogador").get(0).isNull()).isTrue();
        assertThat(estado.get("slotsInimigo").get(0).isNull()).isTrue();
        assertThat(estado.get("maoJogador")).hasSize(4);
        assertThat(estado.get("zonasEfeitoJogador")).hasSize(3);
        assertThat(estado.get("zonasEfeitoInimigo")).hasSize(3);
        assertThat(estado.get("magiaAtivaJogador").isNull()).isTrue();
        assertThat(estado.get("armadilhaJogadorAtiva").asBoolean()).isFalse();
        assertThat(estado.get("armadilhaInimigoAtiva").asBoolean()).isFalse();
        assertThat(estado.toString()).doesNotContain("emoji", "tipoEmoji");

        JsonNode primeiraCarta = estado.get("maoJogador").get(0);
        assertThat(primeiraCarta.get("imageUrl").asText()).startsWith("/images/cards/");
        assertThat(primeiraCarta.get("cardBackUrl").asText()).isEqualTo("/assets/cards/card_back.png");
    }

    @Test
    void turnoInvocaMonstroECompraCartaParaProximoTurno() throws Exception {
        String roomId = "turno-" + UUID.randomUUID();
        JsonNode estado = iniciarSala(roomId);
        int indiceMonstro = primeiroIndiceMonstro(estado);

        String payload = """
                {
                  "invocacaoMonstro": { "indiceMao": %d, "slotDestino": 0 },
                  "acoesCombate": []
                }
                """.formatted(indiceMonstro);

        String response = mockMvc.perform(post("/api/game/turno/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slotsJogador[0]").exists())
                .andExpect(jsonPath("$.turnoAtual").value(2))
                .andExpect(jsonPath("$.tamanhoMaoJogador").value(4))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).doesNotContain("emoji", "tipoEmoji");
    }

    @Test
    void ataqueDiretoComMonstroInimigoRetornaBadRequest() throws Exception {
        String roomId = "direto-" + UUID.randomUUID();
        JsonNode estado = iniciarSala(roomId);
        int indiceMonstro = primeiroIndiceMonstro(estado);

        mockMvc.perform(post("/api/game/turno/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "invocacaoMonstro": { "indiceMao": %d, "slotDestino": 0 },
                                  "acoesCombate": []
                                }
                                """.formatted(indiceMonstro)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/game/turno/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "acoesCombate": [
                                    {
                                      "slotOrigem": 0,
                                      "modo": "ATAQUE",
                                      "indiceAtaque": 0,
                                      "slotAlvo": 0,
                                      "alvoDiretoLider": true
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void armadilhaInimigaFicaOcultaNoEstadoPublico() throws Exception {
        String roomId = "trap-" + UUID.randomUUID();
        JsonNode estado = iniciarSala(roomId);
        int indiceArmadilha = primeiroIndiceTipo(estado, "ARMADILHA");

        if (indiceArmadilha < 0) return;

        String response = mockMvc.perform(post("/api/game/turno/{roomId}", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "acaoEfeito": { "indiceMao": %d, "slotZona": 0 },
                                  "acoesCombate": []
                                }
                                """.formatted(indiceArmadilha)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode resultado = objectMapper.readTree(response);
        if (resultado.get("armadilhaJogadorAtiva").asBoolean()) {
            assertThat(resultado.get("armadilhaJogador").get("oculta").asBoolean()).isFalse();
        } else {
            assertThat(resultado.get("tamanhoDescarteJogador").asInt()).isGreaterThan(0);
        }
    }

    @Test
    void catalogoPersistenteExpoeSeedEDeckPadrao() throws Exception {
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 'charizard')]").exists());

        mockMvc.perform(get("/api/deck/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(30));
    }

    @Test
    void criaCartaCustomUploadEUsaNoDeckPadrao() throws Exception {
        String id = "teste_custom_" + UUID.randomUUID().toString().replace("-", "");
        String deckOriginal = mockMvc.perform(get("/api/deck/default"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        try {
            mockMvc.perform(post("/api/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "id": "%s",
                                      "nome": "Teste Custom",
                                      "cardType": "MONSTRO",
                                      "tipo": "CHAMA",
                                      "universo": "Teste",
                                      "atk": 40,
                                      "def": 30,
                                      "active": true,
                                      "deckCopies": 0,
                                      "ataques": [
                                        { "nome": "Teste", "custoAura": 0, "bonusAtk": 0 }
                                      ],
                                      "regras": [
                                        {
                                          "trigger": "AO_VENCER_CHOQUE",
                                          "target": "DEFENDER",
                                          "actions": [
                                            { "tipo": "PRESSAO", "valor": 1 }
                                          ]
                                        }
                                      ]
                                    }
                                    """.formatted(id)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "card.png", "image/png", new byte[]{1, 2, 3, 4});
            mockMvc.perform(multipart("/api/cards/{id}/image", id).file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.imageUrl").value(org.hamcrest.Matchers.startsWith("/uploads/cards/")));

            JsonNode deck = objectMapper.readTree(mockMvc.perform(get("/api/deck/default"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString());
            ObjectNode novoDeck = objectMapper.createObjectNode();
            ArrayNode cards = novoDeck.putArray("cards");
            for (JsonNode card : deck.get("cards")) {
                ObjectNode entry = cards.addObject();
                entry.put("id", card.get("id").asText());
                entry.put("active", card.get("id").asText().equals(id));
                entry.put("deckCopies", card.get("id").asText().equals(id) ? 30 : 0);
            }
            novoDeck.put("total", 30);

            mockMvc.perform(put("/api/deck/default")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(novoDeck.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(30));

            String roomId = "custom-" + UUID.randomUUID();
            mockMvc.perform(post("/api/game/iniciar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "roomId": "%s",
                                      "liderId": "MAO"
                                    }
                                    """.formatted(roomId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.maoJogador[0].id").value(id));
        } finally {
            mockMvc.perform(put("/api/deck/default")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(deckOriginal))
                    .andExpect(status().isOk());
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/cards/{id}", id));
        }
    }

    @Test
    void jogadorCriaDeckEIniciaPartidaComEle() throws Exception {
        String playerId = "player_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String token = registrarUsuario(playerId, "senha123");
        playerCollectionService.addCard(playerId, "charizard", 30);
        String deckResponse = criarDeckCharizard(playerId, token, "Deck Charizard");
        String deckId = objectMapper.readTree(deckResponse).get("id").asText();

        mockMvc.perform(get("/api/players/{playerId}/decks", playerId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(deckId));

        mockMvc.perform(post("/api/game/iniciar")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomId": "%s",
                                  "liderId": "MAO",
                                  "playerId": "%s",
                                  "deckId": "%s"
                                }
                                """.formatted("player-deck-" + UUID.randomUUID(), playerId, deckId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maoJogador[0].id").value("charizard"))
                .andExpect(jsonPath("$.maoJogador[1].id").value("charizard"));
    }

    @Test
    void pvpCriaSalaEntraResolveTurnoEProtegeEstadoPrivado() throws Exception {
        String creator = "pvp_a_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String guest = "pvp_b_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String intruder = "pvp_c_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String creatorToken = registrarUsuario(creator, "senha123");
        String guestToken = registrarUsuario(guest, "senha123");
        String intruderToken = registrarUsuario(intruder, "senha123");

        playerCollectionService.addCard(creator, "charizard", 30);
        playerCollectionService.addCard(guest, "charizard", 30);
        playerCollectionService.addCard(intruder, "charizard", 30);
        String creatorDeck = objectMapper.readTree(criarDeck(creator, creatorToken, "Deck PvP A", "charizard")).get("id").asText();
        String guestDeck = objectMapper.readTree(criarDeck(guest, guestToken, "Deck PvP B", "charizard")).get("id").asText();
        String intruderDeck = objectMapper.readTree(criarDeck(intruder, intruderToken, "Deck PvP C", "charizard")).get("id").asText();

        mockMvc.perform(post("/api/pvp/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "deckId": "%s", "liderId": "MAO" }
                                """.formatted(creatorDeck)))
                .andExpect(status().isForbidden());

        String createResponse = mockMvc.perform(post("/api/pvp/rooms")
                        .header("Authorization", bearer(creatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "deckId": "%s", "liderId": "MAO" }
                                """.formatted(creatorDeck)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.side").value("CREATOR"))
                .andExpect(jsonPath("$.inviteUrl").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String code = objectMapper.readTree(createResponse).get("code").asText();

        mockMvc.perform(post("/api/pvp/rooms/{code}/join", code)
                        .header("Authorization", bearer(creatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "deckId": "%s", "liderId": "MAO" }
                                """.formatted(creatorDeck)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/pvp/rooms/{code}/join", code)
                        .header("Authorization", bearer(guestToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "deckId": "%s", "liderId": "MAO" }
                                """.formatted(guestDeck)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.side").value("GUEST"));

        mockMvc.perform(post("/api/pvp/rooms/{code}/join", code)
                        .header("Authorization", bearer(intruderToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "deckId": "%s", "liderId": "MAO" }
                                """.formatted(intruderDeck)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/pvp/rooms/{code}/turn", code)
                        .header("Authorization", bearer(creatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "invocacaoMonstro": { "indiceMao": 0, "slotDestino": 0 },
                                  "acoesCombate": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingSelf").value(true))
                .andExpect(jsonPath("$.pendingOpponent").value(false));

        String resolved = mockMvc.perform(post("/api/pvp/rooms/{code}/turn", code)
                        .header("Authorization", bearer(guestToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "invocacaoMonstro": { "indiceMao": 0, "slotDestino": 0 },
                                  "acoesCombate": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingSelf").value(false))
                .andExpect(jsonPath("$.state.slotsJogador[0].id").value("charizard"))
                .andExpect(jsonPath("$.state.slotsInimigo[0].id").value("charizard"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(resolved).doesNotContain("maoInimigo");

        mockMvc.perform(get("/api/pvp/rooms/{code}", code)
                        .header("Authorization", bearer(intruderToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void pvpSemDeckSelecionadoUsaDeckPadrao() throws Exception {
        String creator = "pvp_d_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String guest = "pvp_e_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String creatorToken = registrarUsuario(creator, "senha123");
        String guestToken = registrarUsuario(guest, "senha123");

        String createResponse = mockMvc.perform(post("/api/pvp/rooms")
                        .header("Authorization", bearer(creatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "liderId": "MAO" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String code = objectMapper.readTree(createResponse).get("code").asText();

        mockMvc.perform(post("/api/pvp/rooms/{code}/join", code)
                        .header("Authorization", bearer(guestToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "liderId": "MAO" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(get("/api/pvp/rooms/{code}", code)
                        .header("Authorization", bearer(creatorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state.maoJogador.length()").value(4))
                .andExpect(jsonPath("$.state.tamanhoDeckJogador").value(26))
                .andExpect(jsonPath("$.state.tamanhoMaoInimigo").value(4));
    }

    @Test
    void pvpEstadoDoOponenteNaoRevelaArmadilhaOculta() throws Exception {
        String creator = "trap_a_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String guest = "trap_b_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        String creatorToken = registrarUsuario(creator, "senha123");
        String guestToken = registrarUsuario(guest, "senha123");

        playerCollectionService.addCard(creator, "contra_ataque", 30);
        playerCollectionService.addCard(guest, "contra_ataque", 30);
        String creatorDeck = objectMapper.readTree(criarDeck(creator, creatorToken, "Trap PvP A", "contra_ataque")).get("id").asText();
        String guestDeck = objectMapper.readTree(criarDeck(guest, guestToken, "Trap PvP B", "contra_ataque")).get("id").asText();

        String code = objectMapper.readTree(mockMvc.perform(post("/api/pvp/rooms")
                        .header("Authorization", bearer(creatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "deckId": "%s", "liderId": "MAO" }
                                """.formatted(creatorDeck)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString()).get("code").asText();

        mockMvc.perform(post("/api/pvp/rooms/{code}/join", code)
                        .header("Authorization", bearer(guestToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "deckId": "%s", "liderId": "MAO" }
                                """.formatted(guestDeck)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/pvp/rooms/{code}/turn", code)
                        .header("Authorization", bearer(creatorToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "acaoEfeito": { "indiceMao": 0, "slotZona": 0 },
                                  "acoesCombate": []
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/pvp/rooms/{code}/turn", code)
                        .header("Authorization", bearer(guestToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "acoesCombate": [] }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state.armadilhaInimigoAtiva").value(true))
                .andExpect(jsonPath("$.state.armadilhaInimigo.oculta").value(true))
                .andExpect(jsonPath("$.state.armadilhaInimigo.nome").value("Carta oculta"));
    }

    private String registrarUsuario(String username, String password) throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(authJson(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(username.toLowerCase()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }

    private String criarDeckCharizard(String playerId, String token, String name) throws Exception {
        return criarDeck(playerId, token, name, "charizard");
    }

    private String criarDeck(String playerId, String token, String name, String cardId) throws Exception {
        return mockMvc.perform(post("/api/players/{playerId}/decks", playerId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "cards": [
                                    { "id": "%s", "deckCopies": 30 }
                                  ]
                                }
                                """.formatted(name, cardId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(30))
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String authJson(String username, String password) {
        return """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private JsonNode iniciarSala(String suffix) throws Exception {
        String roomId = suffix.startsWith("turno-") || suffix.startsWith("direto-") || suffix.startsWith("trap-")
                ? suffix
                : suffix + "-" + UUID.randomUUID();

        String response = mockMvc.perform(post("/api/game/iniciar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roomId": "%s",
                                  "liderId": "MAO"
                                }
                                """.formatted(roomId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response);
    }

    private int primeiroIndiceMonstro(JsonNode estado) {
        int indice = primeiroIndiceTipo(estado, "MONSTRO");
        if (indice < 0) throw new AssertionError("Mao inicial deveria conter ao menos um monstro.");
        return indice;
    }

    private int primeiroIndiceTipo(JsonNode estado, String tipo) {
        JsonNode mao = estado.get("maoJogador");
        for (int i = 0; i < mao.size(); i++) {
            if (tipo.equals(mao.get(i).get("cardType").asText())) {
                return i;
            }
        }
        return -1;
    }
}
