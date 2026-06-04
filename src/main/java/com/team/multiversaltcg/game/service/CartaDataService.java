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

    private static final String SOURCE_CUSTOM = "CUSTOM";

    private final Map<String, Carta> cartas = new HashMap<>();
    private final CardDefinitionRepository repository;
    private final ObjectMapper objectMapper;

    public CartaDataService() {
        this.repository = null;
        this.objectMapper = new ObjectMapper();
    }

    @Autowired
    public CartaDataService(CardDefinitionRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void inicializarPersistencia() {
        if (repository == null) return;
        normalizarCartasPersistidas();
        recarregarDoBanco();
    }

    private void normalizarCartasPersistidas() {
        for (CardDefinition definition : repository.findAll()) {
            boolean changed = false;
            if (definition.getRarity() == null || definition.getRarity().isBlank()) {
                definition.setRarity(defaultRarity(definition.getId(), definition.getCardType()));
                changed = true;
            }
            if (definition.getRaritiesJson() == null || definition.getRaritiesJson().isBlank()) {
                definition.setRaritiesJson(writeJson(normalizeRarities(
                        List.of(),
                        definition.getRarity(),
                        defaultRarity(definition.getId(), definition.getCardType()))));
                changed = true;
            }
            if (definition.getRarityImageUrlsJson() == null || definition.getRarityImageUrlsJson().isBlank()) {
                definition.setRarityImageUrlsJson(writeJson(normalizeRarityImageUrls(
                        Map.of(),
                        definition.getImageUrl(),
                        definitionRarities(definition))));
                changed = true;
            }
            if (changed) {
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

    public List<Carta> getDeckPadrao() {
        if (repository == null) return List.of();

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
