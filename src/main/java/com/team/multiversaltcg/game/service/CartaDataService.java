package com.team.multiversaltcg.game.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.multiversaltcg.game.cards.CardDefinition;
import com.team.multiversaltcg.game.cards.CardDefinitionRepository;
import com.team.multiversaltcg.game.dto.CardAdminDTO;
import com.team.multiversaltcg.game.dto.DeckDefaultDTO;
import com.team.multiversaltcg.game.dto.DeckEntryDTO;
import com.team.multiversaltcg.game.enums.CardRarity;
import com.team.multiversaltcg.game.enums.CardType;
import com.team.multiversaltcg.game.enums.StatusEnum;
import com.team.multiversaltcg.game.enums.TipoEfeito;
import com.team.multiversaltcg.game.enums.TipoUniversal;
import com.team.multiversaltcg.game.enums.TriggerArmadilha;
import com.team.multiversaltcg.game.model.Ataque;
import com.team.multiversaltcg.game.model.Carta;
import com.team.multiversaltcg.game.model.EfeitoRegraDeclarativa;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CartaDataService {

    private static final String SOURCE_BASE = "BASE";
    private static final String SOURCE_CUSTOM = "CUSTOM";
    private static final List<String> DEFAULT_DECK_IDS = List.of(
            "charizard", "pikachu", "squirtle", "bulbasaur", "agumon", "garurumon",
            "magonegro", "dragao", "zagueiro", "pontaveloz", "orei", "patamon",
            "evoluir_raichu", "evoluir_blastoise", "evoluir_greymon", "evoluir_wargreymon", "evoluir_angemon",
            "boost_chama", "escudo_abissal", "cura_natureza", "tempestade",
            "portal_sombras", "luz_eterna", "campo_sagrado", "nexo_digital",
            "contra_ataque", "armadilha_explosiva", "barreira_tipo", "julgamento", "espelho_magico"
    );

    private final Map<String, Carta> cartas = new HashMap<>();
    private final CardDefinitionRepository repository;
    private final ObjectMapper objectMapper;

    public CartaDataService() {
        this.repository = null;
        this.objectMapper = new ObjectMapper();
        carregarCartasBase();
    }

    @Autowired
    public CartaDataService(CardDefinitionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void inicializarPersistencia() {
        if (repository == null) return;
        seedBase();
        recarregarDoBanco();
    }

    private void seedBase() {
        Map<String, Carta> base = construirCartasBase();
        if (repository.count() == 0) {
            for (Carta carta : base.values()) {
                int copias = DEFAULT_DECK_IDS.contains(carta.getId()) ? 1 : 0;
                repository.save(toDefinition(carta, true, copias, SOURCE_BASE));
            }
        }
        for (CardDefinition definition : repository.findAll()) {
            if (definition.getRarity() == null || definition.getRarity().isBlank()) {
                definition.setRarity(defaultRarity(definition.getId(), definition.getCardType()));
                repository.save(definition);
            }
            if (definition.getRaritiesJson() == null || definition.getRaritiesJson().isBlank()) {
                definition.setRaritiesJson(writeJson(normalizeRarities(
                        List.of(),
                        definition.getRarity(),
                        defaultRarity(definition.getId(), definition.getCardType()))));
                repository.save(definition);
            }
            if (definition.getRarityImageUrlsJson() == null || definition.getRarityImageUrlsJson().isBlank()) {
                definition.setRarityImageUrlsJson(writeJson(normalizeRarityImageUrls(
                        Map.of(),
                        definition.getImageUrl(),
                        definitionRarities(definition))));
                repository.save(definition);
            }
        }
    }

    private void recarregarDoBanco() {
        cartas.clear();
        for (CardDefinition definition : repository.findAll()) {
            Carta carta = toCarta(definition);
            if (carta != null) cartas.put(carta.getId(), carta);
        }
    }

    private void carregarCartasBase() {
        cartas.clear();
        cartas.putAll(construirCartasBase());
    }

    private Map<String, Carta> construirCartasBase() {
        Map<String, Carta> base = new HashMap<>();
        carregarMonstros(base);
        carregarEvolucoes(base);
        carregarMagias(base);
        carregarArmadilhas(base);
        carregarAliasesLegados(base);
        return base;
    }

    private void carregarMonstros(Map<String, Carta> destino) {
        monstro(destino, "charizard", "Charizard", TipoUniversal.CHAMA, "Pokemon", 90, 45, null,
                "/images/cards/monsters/charizard.png",
                List.of(ataque("Brasa", 2, 0),
                        ataqueStatus("Labareda Furiosa", 4, 20, StatusEnum.QUEIMADO, 2),
                        ataqueStatus("Inferno Supremo", 7, 50, StatusEnum.QUEIMADO, 2)));
        monstro(destino, "pikachu", "Pikachu", TipoUniversal.RELAMPAGO, "Pokemon", 70, 28, "raichu",
                "/images/cards/monsters/pikachu.png",
                List.of(ataque("Choque", 1, 0),
                        ataque("Raio Duplo", 3, 15),
                        ataqueStatus("Tempestade Eletrica", 6, 40, StatusEnum.CONFUSO, 2)));
        monstro(destino, "squirtle", "Squirtle", TipoUniversal.ABISMO, "Pokemon", 45, 68, "blastoise",
                "/images/cards/monsters/squirtle.png",
                List.of(ataque("Bolha", 1, 0),
                        ataque("Hidrojato", 3, 10),
                        ataqueStatus("Maremoto", 6, 30, StatusEnum.CONGELADO, 1)));
        monstro(destino, "bulbasaur", "Bulbasaur", TipoUniversal.NATUREZA, "Pokemon", 50, 55, null,
                "/images/cards/monsters/bulbasaur.png",
                List.of(ataque("Chicote de Vinha", 2, 0),
                        ataqueStatus("Po Venenoso", 3, 0, StatusEnum.ENVENENADO, 4),
                        ataqueStatus("Esporulacao", 5, 20, StatusEnum.ENVENENADO, 4)));
        monstro(destino, "agumon", "Agumon", TipoUniversal.CHAMA, "Digimon", 68, 52, "greymon",
                "/images/cards/monsters/agumon.png",
                List.of(ataque("Bolinha de Fogo", 2, 0),
                        ataqueStatus("Pepper Breath", 4, 25, StatusEnum.QUEIMADO, 2),
                        ataque("Nova Chama", 7, 45)));
        monstro(destino, "garurumon", "Garurumon", TipoUniversal.ABISMO, "Digimon", 88, 68, null,
                "/images/cards/monsters/garurumon.png",
                List.of(ataque("Garras", 2, 0),
                        ataqueStatus("Uivo Gelido", 4, 20, StatusEnum.CONGELADO, 1),
                        ataqueStatus("Blizzard Mortal", 8, 55, StatusEnum.CONGELADO, 1)));
        monstro(destino, "magonegro", "Mago Negro", TipoUniversal.SOMBRA, "Yu-Gi-Oh", 105, 42, null,
                "/images/cards/monsters/mago_negro.png",
                List.of(ataque("Varinha", 3, 0),
                        ataqueStatus("Magia Negra", 5, 30, StatusEnum.CONFUSO, 2),
                        ataqueStatus("Aniquilacao Arcana", 8, 70, StatusEnum.CONFUSO, 2)));
        monstro(destino, "dragao", "Dragao Azul", TipoUniversal.ETER, "Yu-Gi-Oh", 125, 28, null,
                "/images/cards/monsters/dragao_azul.png",
                List.of(ataque("Sopro Divino", 3, 0),
                        ataque("Rajada Celestial", 5, 40),
                        ataque("Aniquilacao Total", 9, 80)));
        monstro(destino, "zagueiro", "Zagueiro Muro", TipoUniversal.ABISMO, "Futebol", 42, 95, null,
                "/images/cards/monsters/zagueiro_muro.png",
                List.of(ataque("Carrinho", 2, 0),
                        ataque("Barreira Viva", 3, 0),
                        ataque("Muro Absoluto", 6, 15)));
        monstro(destino, "pontaveloz", "Ponta Veloz", TipoUniversal.RELAMPAGO, "Futebol", 75, 25, null,
                "/images/cards/monsters/ponta_veloz.png",
                List.of(ataque("Drible", 2, 0),
                        ataque("Chute de Trivela", 4, 30),
                        ataque("Finalizacao Olimpica", 6, 50)));
        monstro(destino, "orei", "O Rei", TipoUniversal.CHAMA, "Figuras BR", 74, 62, null,
                "/images/cards/monsters/o_rei.png",
                List.of(ataque("Gol de Placa", 3, 0),
                        ataque("Drible do Rei", 4, 15),
                        ataque("Rei em Campo", 7, 40)));
        monstro(destino, "patamon", "Patamon", TipoUniversal.ETER, "Digimon", 52, 48, "angemon",
                "/images/cards/monsters/patamon.png",
                List.of(ataque("Boom Bubble", 1, 0),
                        ataque("Heavenly Knuckle", 3, 15),
                        ataqueStatus("Angel Barrage", 5, 30, StatusEnum.CONGELADO, 1)));

        monstro(destino, "raichu", "Raichu", TipoUniversal.RELAMPAGO, "Pokemon", 92, 48, null,
                "/images/cards/evolutions/raichu.png",
                List.of(ataque("Impacto Eletrico", 2, 10),
                        ataqueStatus("Trovao Cruzado", 5, 35, StatusEnum.CONFUSO, 2),
                        ataque("Tempestade Final", 8, 70)));
        monstro(destino, "blastoise", "Blastoise", TipoUniversal.ABISMO, "Pokemon", 65, 112, null,
                "/images/cards/evolutions/blastoise.png",
                List.of(ataque("Canhao de Agua", 3, 10),
                        ataqueStatus("Tsunami Blindado", 6, 35, StatusEnum.CONGELADO, 1),
                        ataque("Fortaleza Abissal", 8, 60)));
        monstro(destino, "greymon", "Greymon", TipoUniversal.CHAMA, "Digimon", 90, 66, "wargreymon",
                "/images/cards/evolutions/greymon.png",
                List.of(ataque("Garra Flamejante", 3, 10),
                        ataqueStatus("Mega Flame", 6, 40, StatusEnum.QUEIMADO, 2),
                        ataque("Chama Primal", 9, 75)));
        monstro(destino, "wargreymon", "WarGreymon", TipoUniversal.CHAMA, "Digimon", 118, 76, null,
                "/images/cards/evolutions/wargreymon.png",
                List.of(ataque("Dramon Killer", 4, 20),
                        ataqueStatus("Gaia Force", 7, 55, StatusEnum.QUEIMADO, 2),
                        ataque("Terra Destroyer", 9, 90)));
        monstro(destino, "angemon", "Angemon", TipoUniversal.ETER, "Digimon", 80, 65, null,
                "/images/cards/evolutions/angemon.png",
                List.of(ataque("Luz Sagrada", 3, 15),
                        ataqueStatus("Punho Celestial", 5, 35, StatusEnum.CONGELADO, 1),
                        ataque("Julgamento Angelical", 8, 70)));
    }

    private void carregarEvolucoes(Map<String, Carta> destino) {
        evolucao(destino, "evoluir_raichu", "Pedra Trovao", "Evolui Pikachu para Raichu.",
                "pikachu", "raichu", "/images/cards/evolutions/raichu.png");
        evolucao(destino, "evoluir_blastoise", "Evolucao Aquatica", "Evolui Squirtle para Blastoise.",
                "squirtle", "blastoise", "/images/cards/evolutions/blastoise.png");
        evolucao(destino, "evoluir_greymon", "Digivolucao Champion", "Evolui Agumon para Greymon.",
                "agumon", "greymon", "/images/cards/evolutions/greymon.png");
        evolucao(destino, "evoluir_wargreymon", "Digivolucao Ultimate", "Evolui Greymon para WarGreymon.",
                "greymon", "wargreymon", "/images/cards/evolutions/wargreymon.png");
        evolucao(destino, "evoluir_angemon", "Digivolucao Celestial", "Evolui Patamon para Angemon.",
                "patamon", "angemon", "/images/cards/evolutions/angemon.png");
    }

    private void carregarMagias(Map<String, Carta> destino) {
        magia(destino, "boost_chama", "Boost de Chama", "Monstros Chama aliados recebem +10 ATK por turno.",
                TipoEfeito.BOOST_ATK_TIPO, 0, 10, -1, TipoUniversal.CHAMA, "/images/cards/magics/boost_chama.png");
        magia(destino, "escudo_abissal", "Escudo Abissal", "Reduz Pressao recebida e expira em 3 turnos.",
                TipoEfeito.SHIELD_PRESSAO, 0, 1, 3, TipoUniversal.ABISMO, "/images/cards/magics/escudo_abissal.png");
        magia(destino, "cura_natureza", "Cura Natureza", "Remove status e cura Pressao por turno.",
                TipoEfeito.CURA_STATUS, 0, 1, -1, TipoUniversal.NATUREZA, "/images/cards/magics/cura_natureza.png");
        magia(destino, "tempestade", "Tempestade", "Compre 2 cartas e absorva Aura extra em vitorias.",
                TipoEfeito.DRAW_CARTAS, 0, 2, -1, TipoUniversal.RELAMPAGO, "/images/cards/magics/tempestade.png");
        magia(destino, "portal_sombras", "Portal das Sombras", "Remove bonus defensivos e ignora defesa por 2 turnos.",
                TipoEfeito.IGNORAR_DEFESA, 0, 1, 2, TipoUniversal.SOMBRA, "/images/cards/magics/portal_sombras.png");
        magia(destino, "luz_eterna", "Luz Eterna", "Cura Pressao e da imunidade a monstros Eter.",
                TipoEfeito.IMUNIDADE_STATUS, 0, 1, -1, TipoUniversal.ETER, "/images/cards/magics/luz_eterna.png");
        magia(destino, "campo_sagrado", "Campo Sagrado", "Farm gera +1 Aura extra.",
                TipoEfeito.BOOST_AURA_FARM, 0, 1, -1, null, "/images/cards/magics/campo_sagrado.png");
        magia(destino, "nexo_digital", "Nexo Digital", "Escolha 1 das 3 cartas do topo; evolucoes ficam gratis.",
                TipoEfeito.BUSCA_DECK, 0, 1, 3, null, "/images/cards/magics/nexo_digital.png");
    }

    private void carregarArmadilhas(Map<String, Carta> destino) {
        armadilha(destino, "contra_ataque", "Contra-Ataque", "Quando o inimigo ataca, aplica Pressao ao atacante.",
                TriggerArmadilha.INIMIGO_ATACA, TipoEfeito.PRESSAO_ALVO, 1, "/images/cards/traps/contra_ataque.png");
        armadilha(destino, "armadilha_explosiva", "Armadilha Explosiva", "Monstro inimigo invocado entra pressionado.",
                TriggerArmadilha.INIMIGO_INVOCA, TipoEfeito.PRESSAO_ALVO, 1, "/images/cards/traps/armadilha_explosiva.png");
        armadilha(destino, "barreira_tipo", "Barreira de Tipo", "Cancela multiplicador de vantagem naquele choque.",
                TriggerArmadilha.INIMIGO_USA_VANTAGEM_TIPO, TipoEfeito.BARREIRA_TIPO, 0, "/images/cards/traps/barreira_tipo.png");
        armadilha(destino, "julgamento", "Julgamento", "Antes do KO, aplica 2 Pressoes no inimigo mais pressionado.",
                TriggerArmadilha.SEU_MONSTRO_KO, TipoEfeito.JULGAMENTO, 2, "/images/cards/traps/julgamento.png");
        armadilha(destino, "espelho_magico", "Espelho Magico", "Anula uma magia inimiga.",
                TriggerArmadilha.INIMIGO_JOGA_MAGIA, TipoEfeito.ESPELHO_MAGICO, 0, "/images/cards/traps/espelho_magico.png");
    }

    private void carregarAliasesLegados(Map<String, Carta> destino) {
        magia(destino, "fonte_aura", "Fonte de Aura", "Ganha 4 de Aura.", TipoEfeito.AURA, 0, 4, 0, null,
                "/images/cards/magics/campo_sagrado.png");
        magia(destino, "cura_tatica", "Cura Tatica", "Recupera 8 HP do lider.", TipoEfeito.CURAR_LIDER, 0, 8, 0, null,
                "/images/cards/magics/cura_natureza.png");
        magia(destino, "forja_atk", "Forja de Ataque", "Um monstro aliado recebe +20 ATK.", TipoEfeito.BUFF_ATK, 2, 20, 0, null,
                "/images/cards/magics/boost_chama.png");
        magia(destino, "muralha_def", "Muralha de Defesa", "Um monstro aliado recebe +20 DEF.", TipoEfeito.BUFF_DEF, 2, 20, 0, null,
                "/images/cards/magics/escudo_abissal.png");
        armadilha(destino, "escudo_reativo", "Escudo Reativo", "Quando o inimigo ataca, cancela esse ataque.",
                TriggerArmadilha.INIMIGO_ATACA, TipoEfeito.CANCELAR_ATAQUE, 0, "/images/cards/traps/barreira_tipo.png");
        armadilha(destino, "buraco_instavel", "Buraco Instavel", "Invocar ou atacar aplica 1 Pressao.",
                TriggerArmadilha.AMBOS, TipoEfeito.PRESSAO_ALVO, 1, "/images/cards/traps/armadilha_explosiva.png");
        evolucao(destino, "evoluir_venusaur", "Evolucao: Venusaur", "Alias legado sem uso no deck.",
                "bulbasaur", "bulbasaur", "/images/cards/evolutions/angemon.png");
    }

    public Carta getById(String id) {
        if (repository != null) {
            CardDefinition definition = repository.findById(id).orElse(null);
            Carta carta = toCarta(definition);
            return carta == null ? null : carta.copy();
        }
        Carta carta = cartas.get(id);
        return carta == null ? null : carta.copy();
    }

    public List<Carta> getTodos() {
        if (repository != null) {
            return repository.findAllByOrderByNomeAsc().stream()
                    .map(this::toCarta)
                    .filter(c -> c != null)
                    .map(Carta::copy)
                    .toList();
        }
        return cartas.values().stream().map(Carta::copy).toList();
    }

    public List<Carta> getMonstrosPadrao() {
        return ids(List.of("charizard", "pikachu", "squirtle", "bulbasaur", "agumon", "garurumon",
                "magonegro", "dragao", "zagueiro", "pontaveloz", "orei", "patamon"));
    }

    public List<Carta> getMagiasPadrao() {
        return ids(List.of("boost_chama", "escudo_abissal", "cura_natureza", "tempestade",
                "portal_sombras", "luz_eterna", "campo_sagrado", "nexo_digital"));
    }

    public List<Carta> getArmadilhasPadrao() {
        return ids(List.of("contra_ataque", "armadilha_explosiva", "barreira_tipo", "julgamento", "espelho_magico"));
    }

    public List<Carta> getEvolucoesPadrao() {
        return ids(List.of("evoluir_raichu", "evoluir_blastoise", "evoluir_greymon",
                "evoluir_wargreymon", "evoluir_angemon"));
    }

    public List<Carta> getDeckPadrao() {
        if (repository == null) return ids(DEFAULT_DECK_IDS);

        List<Carta> deck = new ArrayList<>();
        for (CardDefinition definition : repository.findByActiveTrueAndDeckCopiesGreaterThanOrderByNomeAsc(0)) {
            Carta carta = toCarta(definition);
            if (carta == null) continue;
            for (int i = 0; i < definition.getDeckCopies(); i++) {
                deck.add(carta.copy());
            }
        }
        return deck;
    }

    public List<Carta> montarDeck(List<DeckEntryDTO> entries) {
        if (entries == null) throw new RegraInvalidaException("Deck sem cartas.");
        int total = entries.stream().mapToInt(DeckEntryDTO::getDeckCopies).sum();
        if (total != 30) {
            throw new RegraInvalidaException("Deck deve ter exatamente 30 cartas. Total atual: " + total);
        }
        List<Carta> deck = new ArrayList<>();
        for (DeckEntryDTO entry : entries) {
            if (entry.getDeckCopies() <= 0) continue;
            Carta carta = getById(entry.getId());
            if (carta == null) throw new RegraInvalidaException("Carta nao encontrada no deck: " + entry.getId());
            for (int i = 0; i < entry.getDeckCopies(); i++) {
                deck.add(carta.copy());
            }
        }
        return deck;
    }

    public List<CardAdminDTO> listarAdmin() {
        exigirRepositorio();
        return repository.findAllByOrderByNomeAsc().stream().map(this::toAdminDTO).toList();
    }

    public List<CardAdminDTO> listarAtivasAdmin() {
        exigirRepositorio();
        return repository.findAllByOrderByNomeAsc().stream()
                .filter(CardDefinition::isActive)
                .map(this::toAdminDTO)
                .toList();
    }

    public CardAdminDTO buscarAdmin(String id) {
        exigirRepositorio();
        return repository.findById(id).map(this::toAdminDTO)
                .orElseThrow(() -> new RegraInvalidaException("Carta nao encontrada: " + id));
    }

    public CardAdminDTO salvar(CardAdminDTO dto, String idForcado) {
        exigirRepositorio();
        CardDefinition definition = toDefinition(dto, idForcado);
        repository.save(definition);
        recarregarDoBanco();
        return toAdminDTO(definition);
    }

    public void excluir(String id) {
        exigirRepositorio();
        CardDefinition definition = repository.findById(id)
                .orElseThrow(() -> new RegraInvalidaException("Carta nao encontrada: " + id));
        repository.delete(definition);
        recarregarDoBanco();
    }

    public CardAdminDTO atualizarImagem(String id, String imageUrl) {
        return atualizarImagem(id, imageUrl, null);
    }

    public CardAdminDTO atualizarImagem(String id, String imageUrl, String rarity) {
        exigirRepositorio();
        CardDefinition definition = repository.findById(id)
                .orElseThrow(() -> new RegraInvalidaException("Carta nao encontrada: " + id));
        List<String> rarities = definitionRarities(definition);
        Map<String, String> imageUrls = new LinkedHashMap<>(definitionRarityImageUrls(definition, rarities));
        String parsedRarity = enumName(CardRarity.class, rarity);
        if (parsedRarity == null) {
            parsedRarity = rarities.get(0);
        }
        if (!rarities.contains(parsedRarity)) {
            throw new RegraInvalidaException("A carta nao possui a raridade " + parsedRarity + ".");
        }

        imageUrls.put(parsedRarity, imageUrl);
        if (parsedRarity.equals(rarities.get(0)) || definition.getImageUrl() == null || definition.getImageUrl().isBlank()) {
            definition.setImageUrl(imageUrl);
        }
        definition.setRarityImageUrlsJson(writeJson(imageUrls));
        repository.save(definition);
        recarregarDoBanco();
        return toAdminDTO(definition);
    }

    public CardAdminDTO comRaridade(CardAdminDTO card, String rarity) {
        String parsedRarity = enumName(CardRarity.class, rarity);
        List<String> rarities = normalizeRarities(card.getRarities(), card.getRarity(), card.getRarity());
        if (parsedRarity == null || !rarities.contains(parsedRarity)) {
            parsedRarity = card.getRarity();
        }
        String imageUrl = imageUrlFor(card.getRarityImageUrls(), card.getImageUrl(), parsedRarity);
        return CardAdminDTO.builder()
                .id(card.getId())
                .nome(card.getNome())
                .descricao(card.getDescricao())
                .imageUrl(imageUrl == null || imageUrl.isBlank() ? Carta.PLACEHOLDER_IMAGE_URL : imageUrl)
                .rarityImageUrls(card.getRarityImageUrls())
                .cardType(card.getCardType())
                .rarity(parsedRarity)
                .rarities(rarities)
                .tipo(card.getTipo())
                .universo(card.getUniverso())
                .atk(card.getAtk())
                .def(card.getDef())
                .evolucaoId(card.getEvolucaoId())
                .efeito(card.getEfeito())
                .trigger(card.getTrigger())
                .custoAura(card.getCustoAura())
                .valor(card.getValor())
                .duracao(card.getDuracao())
                .tipoAlvo(card.getTipoAlvo())
                .baseMonsterId(card.getBaseMonsterId())
                .evolvedMonsterId(card.getEvolvedMonsterId())
                .ataques(card.getAtaques())
                .regras(card.getRegras())
                .active(card.isActive())
                .deckCopies(card.getDeckCopies())
                .source(card.getSource())
                .build();
    }

    public DeckDefaultDTO getDeckDefaultDTO() {
        exigirRepositorio();
        List<DeckEntryDTO> entries = repository.findAllByOrderByNomeAsc().stream()
                .map(d -> DeckEntryDTO.builder()
                        .id(d.getId())
                        .nome(d.getNome())
                        .cardType(d.getCardType())
                        .rarity(d.getRarity())
                        .imageUrl(d.getImageUrl() == null || d.getImageUrl().isBlank()
                                ? Carta.PLACEHOLDER_IMAGE_URL
                                : d.getImageUrl())
                        .active(d.isActive())
                        .deckCopies(d.getDeckCopies())
                        .build())
                .toList();
        return DeckDefaultDTO.builder()
                .total(entries.stream().mapToInt(DeckEntryDTO::getDeckCopies).sum())
                .cards(entries)
                .build();
    }

    public DeckDefaultDTO salvarDeckDefault(DeckDefaultDTO dto) {
        exigirRepositorio();
        int total = dto.getCards() == null ? 0 : dto.getCards().stream().mapToInt(DeckEntryDTO::getDeckCopies).sum();
        if (total != 30) {
            throw new RegraInvalidaException("Deck padrao deve ter exatamente 30 cartas. Total atual: " + total);
        }
        for (DeckEntryDTO entry : dto.getCards()) {
            CardDefinition definition = repository.findById(entry.getId())
                    .orElseThrow(() -> new RegraInvalidaException("Carta nao encontrada no deck: " + entry.getId()));
            int copies = Math.max(0, entry.getDeckCopies());
            definition.setDeckCopies(copies);
            definition.setActive(copies > 0 || entry.isActive());
            repository.save(definition);
        }
        recarregarDoBanco();
        return getDeckDefaultDTO();
    }

    public Map<String, Object> getOptions() {
        return Map.of(
                "cardTypes", CardType.values(),
                "rarities", CardRarity.values(),
                "tipos", TipoUniversal.values(),
                "efeitos", TipoEfeito.values(),
                "triggers", TriggerArmadilha.values(),
                "status", StatusEnum.values(),
                "ruleTriggers", com.team.multiversaltcg.game.enums.EfeitoTrigger.values(),
                "ruleTargets", com.team.multiversaltcg.game.enums.EfeitoAlvo.values(),
                "ruleActions", com.team.multiversaltcg.game.enums.EfeitoAcaoTipo.values()
        );
    }

    private List<Carta> ids(List<String> ids) {
        List<Carta> resultado = new ArrayList<>();
        for (String id : ids) {
            Carta carta = getById(id);
            if (carta != null) resultado.add(carta);
        }
        return resultado;
    }

    private CardDefinition toDefinition(CardAdminDTO dto, String idForcado) {
        boolean criando = idForcado == null || idForcado.isBlank();
        String id = criando
                ? (isBlank(dto.getId()) ? uniqueIdFor(dto.getNome()) : dto.getId())
                : idForcado;
        if (id == null || id.isBlank()) throw new RegraInvalidaException("Nome da carta e obrigatorio.");

        CardType cardType = enumValue(CardType.class, dto.getCardType(), "cardType");
        validar(dto, cardType);
        List<String> rarities = normalizeRarities(dto.getRarities(), dto.getRarity(), defaultRarity(id, cardType.name()));
        Map<String, String> rarityImageUrls = normalizeRarityImageUrls(dto.getRarityImageUrls(), dto.getImageUrl(), rarities);
        String primaryImageUrl = imageUrlFor(rarityImageUrls, dto.getImageUrl(), rarities.get(0));

        CardDefinition atual = repository.findById(id).orElse(null);
        String source = atual == null ? SOURCE_CUSTOM : atual.getSource();
        if (source == null || source.isBlank()) source = SOURCE_CUSTOM;

        return CardDefinition.builder()
                .id(id)
                .nome(dto.getNome())
                .descricao(dto.getDescricao())
                .imageUrl(blankToNull(primaryImageUrl))
                .cardType(cardType.name())
                .rarityImageUrlsJson(writeJson(rarityImageUrls))
                .rarity(rarities.get(0))
                .raritiesJson(writeJson(rarities))
                .tipo(enumName(TipoUniversal.class, dto.getTipo()))
                .universo(dto.getUniverso())
                .atk(dto.getAtk())
                .def(dto.getDef())
                .evolucaoId(blankToNull(dto.getEvolucaoId()))
                .efeito(enumName(TipoEfeito.class, dto.getEfeito()))
                .trigger(enumName(TriggerArmadilha.class, dto.getTrigger()))
                .custoAura(dto.getCustoAura())
                .valor(dto.getValor())
                .duracao(dto.getDuracao())
                .tipoAlvo(enumName(TipoUniversal.class, dto.getTipoAlvo()))
                .baseMonsterId(blankToNull(dto.getBaseMonsterId()))
                .evolvedMonsterId(blankToNull(dto.getEvolvedMonsterId()))
                .ataquesJson(writeJson(dto.getAtaques() == null ? List.of() : dto.getAtaques()))
                .regrasJson(writeJson(dto.getRegras() == null ? List.of() : dto.getRegras()))
                .active(dto.isActive())
                .deckCopies(Math.max(0, dto.getDeckCopies()))
                .source(source)
                .build();
    }

    private CardDefinition toDefinition(Carta carta, boolean active, int deckCopies, String source) {
        String rarity = carta.getRarity() == null
                ? defaultRarity(carta.getId(), carta.getCardType().name())
                : carta.getRarity().name();
        Map<String, String> rarityImageUrls = normalizeRarityImageUrls(
                carta.getRarityImageUrls(),
                carta.getImageUrl(),
                List.of(rarity));
        return CardDefinition.builder()
                .id(carta.getId())
                .nome(carta.getNome())
                .descricao(carta.getDescricao())
                .imageUrl(imageUrlFor(rarityImageUrls, carta.getImageUrl(), rarity))
                .cardType(carta.getCardType().name())
                .rarityImageUrlsJson(writeJson(rarityImageUrls))
                .rarity(rarity)
                .raritiesJson(writeJson(List.of(rarity)))
                .tipo(carta.getTipo() == null ? null : carta.getTipo().name())
                .universo(carta.getUniverso())
                .atk(carta.getAtk())
                .def(carta.getDef())
                .evolucaoId(carta.getEvolucaoId())
                .efeito(carta.getEfeito() == null ? null : carta.getEfeito().name())
                .trigger(carta.getTrigger() == null ? null : carta.getTrigger().name())
                .custoAura(carta.getCustoAura())
                .valor(carta.getValor())
                .duracao(carta.getDuracao())
                .tipoAlvo(carta.getTipoAlvo() == null ? null : carta.getTipoAlvo().name())
                .baseMonsterId(carta.getBaseMonsterId())
                .evolvedMonsterId(carta.getEvolvedMonsterId())
                .ataquesJson(writeJson(carta.getAtaques() == null ? List.of() : carta.getAtaques()))
                .regrasJson(writeJson(carta.getRegras() == null ? List.of() : carta.getRegras()))
                .active(active)
                .deckCopies(deckCopies)
                .source(source)
                .build();
    }

    private Carta toCarta(CardDefinition definition) {
        if (definition == null) return null;
        CardType cardType = enumValue(CardType.class, definition.getCardType(), "cardType");
        List<String> rarities = definitionRarities(definition);
        Map<String, String> rarityImageUrls = definitionRarityImageUrls(definition, rarities);
        CardRarity rarity = enumNullable(CardRarity.class, rarities.get(0));
        TipoUniversal tipo = enumNullable(TipoUniversal.class, definition.getTipo());
        TipoEfeito efeito = enumNullable(TipoEfeito.class, definition.getEfeito());
        TriggerArmadilha trigger = enumNullable(TriggerArmadilha.class, definition.getTrigger());
        TipoUniversal tipoAlvo = enumNullable(TipoUniversal.class, definition.getTipoAlvo());
        List<Ataque> ataques = readJson(definition.getAtaquesJson(), new TypeReference<>() {}, List.of());
        List<EfeitoRegraDeclarativa> regras = readJson(definition.getRegrasJson(), new TypeReference<>() {}, List.of());
        return Carta.builder()
                .id(definition.getId())
                .nome(definition.getNome())
                .descricao(definition.getDescricao())
                .imageUrl(imageUrlFor(rarityImageUrls, definition.getImageUrl(), rarities.get(0)))
                .rarityImageUrls(rarityImageUrls)
                .cardType(cardType)
                .rarity(rarity == null
                        ? enumValue(CardRarity.class, defaultRarity(definition.getId(), definition.getCardType()), "rarity")
                        : rarity)
                .tipo(tipo)
                .universo(definition.getUniverso())
                .atk(definition.getAtk())
                .def(definition.getDef())
                .ataques(ataques)
                .evolucaoId(definition.getEvolucaoId())
                .efeito(efeito)
                .trigger(trigger)
                .custoAura(definition.getCustoAura())
                .valor(definition.getValor())
                .duracao(definition.getDuracao())
                .turnosRestantes(definition.getDuracao())
                .tipoAlvo(tipoAlvo)
                .baseMonsterId(definition.getBaseMonsterId())
                .evolvedMonsterId(definition.getEvolvedMonsterId())
                .regras(regras)
                .build();
    }

    private CardAdminDTO toAdminDTO(CardDefinition definition) {
        List<String> rarities = definitionRarities(definition);
        Map<String, String> rarityImageUrls = definitionRarityImageUrls(definition, rarities);
        String imageUrl = imageUrlFor(rarityImageUrls, definition.getImageUrl(), rarities.get(0));
        return CardAdminDTO.builder()
                .id(definition.getId())
                .nome(definition.getNome())
                .descricao(definition.getDescricao())
                .imageUrl(imageUrl == null || imageUrl.isBlank()
                        ? Carta.PLACEHOLDER_IMAGE_URL
                        : imageUrl)
                .rarityImageUrls(rarityImageUrls)
                .cardType(definition.getCardType())
                .rarity(rarities.get(0))
                .rarities(rarities)
                .tipo(definition.getTipo())
                .universo(definition.getUniverso())
                .atk(definition.getAtk())
                .def(definition.getDef())
                .evolucaoId(definition.getEvolucaoId())
                .efeito(definition.getEfeito())
                .trigger(definition.getTrigger())
                .custoAura(definition.getCustoAura())
                .valor(definition.getValor())
                .duracao(definition.getDuracao())
                .tipoAlvo(definition.getTipoAlvo())
                .baseMonsterId(definition.getBaseMonsterId())
                .evolvedMonsterId(definition.getEvolvedMonsterId())
                .ataques(readJson(definition.getAtaquesJson(), new TypeReference<>() {}, List.of()))
                .regras(readJson(definition.getRegrasJson(), new TypeReference<>() {}, List.of()))
                .active(definition.isActive())
                .deckCopies(definition.getDeckCopies())
                .source(definition.getSource())
                .build();
    }

    private void validar(CardAdminDTO dto, CardType cardType) {
        if (dto.getNome() == null || dto.getNome().isBlank()) {
            throw new RegraInvalidaException("Nome da carta e obrigatorio.");
        }
        if (cardType == CardType.MONSTRO) {
            enumValue(TipoUniversal.class, dto.getTipo(), "tipo");
            if (dto.getAtk() <= 0 || dto.getDef() <= 0) {
                throw new RegraInvalidaException("Monstros precisam de ATK e DEF maiores que zero.");
            }
            if (dto.getAtaques() == null || dto.getAtaques().isEmpty()) {
                throw new RegraInvalidaException("Monstros precisam de ao menos 1 ataque.");
            }
        }
        if (cardType == CardType.EVOLUCAO
                && (isBlank(dto.getBaseMonsterId()) || isBlank(dto.getEvolvedMonsterId()))) {
            throw new RegraInvalidaException("Evolucoes precisam de baseMonsterId e evolvedMonsterId.");
        }
        if ((cardType == CardType.MAGIA || cardType == CardType.ARMADILHA)
                && isBlank(dto.getEfeito())
                && (dto.getRegras() == null || dto.getRegras().isEmpty())) {
            throw new RegraInvalidaException("Magias e armadilhas precisam de efeito existente ou regra declarativa.");
        }
        if (cardType == CardType.ARMADILHA && isBlank(dto.getTrigger())) {
            throw new RegraInvalidaException("Armadilhas precisam de trigger.");
        }
    }

    private void monstro(Map<String, Carta> destino, String id, String nome, TipoUniversal tipo, String universo,
                         int atk, int def, String evolucaoId, String imageUrl, List<Ataque> ataques) {
        add(destino, Carta.builder()
                .id(id).nome(nome).cardType(CardType.MONSTRO).tipo(tipo).universo(universo)
                .atk(atk).def(def).evolucaoId(evolucaoId).imageUrl(imageUrl).ataques(ataques)
                .build());
    }

    private void magia(Map<String, Carta> destino, String id, String nome, String descricao, TipoEfeito efeito,
                       int custoAura, int valor, int duracao, TipoUniversal tipoAlvo, String imageUrl) {
        add(destino, Carta.builder()
                .id(id).nome(nome).descricao(descricao).cardType(CardType.MAGIA)
                .efeito(efeito).custoAura(custoAura).valor(valor).duracao(duracao)
                .turnosRestantes(duracao).tipoAlvo(tipoAlvo).imageUrl(imageUrl)
                .build());
    }

    private void armadilha(Map<String, Carta> destino, String id, String nome, String descricao,
                           TriggerArmadilha trigger, TipoEfeito efeito, int valor, String imageUrl) {
        add(destino, Carta.builder()
                .id(id).nome(nome).descricao(descricao).cardType(CardType.ARMADILHA)
                .trigger(trigger).efeito(efeito).valor(valor).imageUrl(imageUrl)
                .build());
    }

    private void evolucao(Map<String, Carta> destino, String id, String nome, String descricao,
                          String baseMonsterId, String evolvedMonsterId, String imageUrl) {
        add(destino, Carta.builder()
                .id(id).nome(nome).descricao(descricao).cardType(CardType.EVOLUCAO)
                .baseMonsterId(baseMonsterId).evolvedMonsterId(evolvedMonsterId).imageUrl(imageUrl)
                .build());
    }

    private Ataque ataque(String nome, int custoAura, int bonusAtk) {
        return Ataque.builder().nome(nome).custoAura(custoAura).bonusAtk(bonusAtk).build();
    }

    private Ataque ataqueStatus(String nome, int custoAura, int bonusAtk,
                                StatusEnum status, int duracao) {
        return Ataque.builder()
                .nome(nome).custoAura(custoAura).bonusAtk(bonusAtk)
                .statusAplicado(status).duracaoStatus(duracao)
                .build();
    }

    private void add(Map<String, Carta> destino, Carta carta) {
        destino.put(carta.getId(), carta);
    }

    private void exigirRepositorio() {
        if (repository == null) {
            throw new RegraInvalidaException("Repositorio de cartas indisponivel.");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new RegraInvalidaException("Falha ao serializar carta: " + ex.getMessage());
        }
    }

    private <T> T readJson(String json, TypeReference<T> type, T fallback) {
        if (json == null || json.isBlank()) return fallback;
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            return fallback;
        }
    }

    private <T extends Enum<T>> T enumValue(Class<T> type, String value, String field) {
        T result = enumNullable(type, value);
        if (result == null) throw new RegraInvalidaException("Valor invalido para " + field + ": " + value);
        return result;
    }

    private <T extends Enum<T>> T enumNullable(Class<T> type, String value) {
        if (value == null || value.isBlank()) return null;
        return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
    }

    private <T extends Enum<T>> String enumName(Class<T> type, String value) {
        T parsed = enumNullable(type, value);
        return parsed == null ? null : parsed.name();
    }

    private Map<String, String> definitionRarityImageUrls(CardDefinition definition, List<String> rarities) {
        Map<String, String> saved = readJson(
                definition.getRarityImageUrlsJson(),
                new TypeReference<Map<String, String>>() {},
                Map.of());
        return normalizeRarityImageUrls(saved, definition.getImageUrl(), rarities);
    }

    private Map<String, String> normalizeRarityImageUrls(Map<String, String> values, String fallback, List<String> rarities) {
        Map<String, String> result = new LinkedHashMap<>();
        List<String> normalizedRarities = rarities == null || rarities.isEmpty() ? List.of(CardRarity.COMUM.name()) : rarities;
        if (values != null) {
            for (String rarity : normalizedRarities) {
                String imageUrl = values.get(rarity);
                if (!isBlank(imageUrl)) {
                    result.put(rarity, imageUrl.trim());
                }
            }
        }
        if (!isBlank(fallback) && !result.containsKey(normalizedRarities.get(0))) {
            result.put(normalizedRarities.get(0), fallback.trim());
        }
        return result;
    }

    private String imageUrlFor(Map<String, String> imageUrls, String fallback, String rarity) {
        if (imageUrls != null && !isBlank(rarity)) {
            String imageUrl = imageUrls.get(rarity);
            if (!isBlank(imageUrl)) {
                return imageUrl;
            }
        }
        return fallback;
    }

    private List<String> definitionRarities(CardDefinition definition) {
        List<String> saved = readJson(definition.getRaritiesJson(), new TypeReference<List<String>>() {}, List.of());
        return normalizeRarities(saved, definition.getRarity(), defaultRarity(definition.getId(), definition.getCardType()));
    }

    private List<String> normalizeRarities(List<String> values, String fallback, String defaultValue) {
        List<String> result = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                String parsed = enumName(CardRarity.class, value);
                if (parsed != null && !result.contains(parsed)) {
                    result.add(parsed);
                }
            }
        }
        String fallbackParsed = enumName(CardRarity.class, fallback);
        if (result.isEmpty() && fallbackParsed != null) {
            result.add(fallbackParsed);
        }
        String defaultParsed = enumName(CardRarity.class, defaultValue);
        if (result.isEmpty() && defaultParsed != null) {
            result.add(defaultParsed);
        }
        if (result.isEmpty()) {
            result.add(CardRarity.COMUM.name());
        }
        return result;
    }

    private String uniqueIdFor(String nome) {
        String base = slug(nome);
        if (base == null || base.isBlank()) {
            return null;
        }
        String candidate = base;
        int suffix = 2;
        while (repository.existsById(candidate)) {
            candidate = base + "_" + suffix;
            suffix++;
        }
        return candidate;
    }

    private String slug(String value) {
        if (value == null) return null;
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return normalized.isBlank() ? null : normalized;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value;
    }

    private String defaultRarity(String id, String cardType) {
        String normalizedId = id == null ? "" : id;
        if (normalizedId.contains("wargreymon") || normalizedId.contains("dragao") || normalizedId.contains("julgamento")) {
            return CardRarity.LENDARIO.name();
        }
        if ("EVOLUCAO".equals(cardType) || normalizedId.contains("magonegro") || normalizedId.contains("nexo")) {
            return CardRarity.EPICO.name();
        }
        if ("ARMADILHA".equals(cardType) || "MAGIA".equals(cardType)) {
            return CardRarity.RARO.name();
        }
        if (normalizedId.contains("charizard") || normalizedId.contains("garurumon") || normalizedId.contains("pikachu")) {
            return CardRarity.INCOMUM.name();
        }
        return CardRarity.COMUM.name();
    }
}
