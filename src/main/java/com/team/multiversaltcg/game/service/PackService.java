package com.team.multiversaltcg.game.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.multiversaltcg.game.dto.CardAdminDTO;
import com.team.multiversaltcg.game.dto.CollectionCardDTO;
import com.team.multiversaltcg.game.dto.PackAdminDTO;
import com.team.multiversaltcg.game.dto.PackOpeningDTO;
import com.team.multiversaltcg.game.dto.PackShopDTO;
import com.team.multiversaltcg.game.dto.PlayerProfileDTO;
import com.team.multiversaltcg.game.dto.ShopDTO;
import com.team.multiversaltcg.game.enums.CardRarity;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.packs.PackDefinition;
import com.team.multiversaltcg.game.packs.PackDefinitionRepository;
import com.team.multiversaltcg.game.packs.PackOpening;
import com.team.multiversaltcg.game.packs.PackOpeningRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PackService {

    private static final int DEFAULT_CARDS_PER_PACK = 5;

    private static final Map<CardRarity, Integer> ODDS = Map.of(
            CardRarity.COMUM, 55,
            CardRarity.INCOMUM, 25,
            CardRarity.RARO, 13,
            CardRarity.EPICO, 5,
            CardRarity.LENDARIO, 2
    );

    private final PackOpeningRepository openingRepository;
    private final PackDefinitionRepository definitionRepository;
    private final CartaDataService cartaDataService;
    private final PlayerCollectionService collectionService;
    private final PlayerProfileService profileService;
    private final ObjectMapper objectMapper;
    private final SecureRandom random = new SecureRandom();

    public PackService(PackOpeningRepository openingRepository,
                       PackDefinitionRepository definitionRepository,
                       CartaDataService cartaDataService,
                       PlayerCollectionService collectionService,
                       PlayerProfileService profileService,
                       ObjectMapper objectMapper) {
        this.openingRepository = openingRepository;
        this.definitionRepository = definitionRepository;
        this.cartaDataService = cartaDataService;
        this.collectionService = collectionService;
        this.profileService = profileService;
        this.objectMapper = objectMapper;
    }

    public List<PackAdminDTO> listarAdmin() {
        Map<String, CardAdminDTO> cardsById = cardsById(cartaDataService.listarAdmin());
        return definitionRepository.findAllByOrderByNomeAsc().stream()
                .map(definition -> toAdminDTO(definition, cardsById))
                .toList();
    }

    public PackAdminDTO buscarAdmin(String id) {
        return toAdminDTO(getDefinition(id));
    }

    public PackAdminDTO salvar(PackAdminDTO dto, String idForcado) {
        PackDefinition definition = toDefinition(dto, idForcado);
        definitionRepository.save(definition);
        return toAdminDTO(definition);
    }

    public void excluir(String id) {
        PackDefinition definition = getDefinition(id);
        definitionRepository.delete(definition);
    }

    public ShopDTO getShop(String playerId) {
        PlayerProfileDTO profile = profileService.ensureProfile(playerId);
        Map<String, CardAdminDTO> cardsById = cardsById(cartaDataService.listarAdmin());
        return ShopDTO.builder()
                .playerId(playerId)
                .coins(profile.getCoins())
                .packs(definitionRepository.findByActiveTrueOrderByNomeAsc().stream()
                        .map(definition -> toShopDTO(definition, cardsById))
                        .toList())
                .odds(odds())
                .build();
    }

    @Transactional
    public PackOpeningDTO buyPack(String playerId, String packId) {
        PackDefinition definition = getDefinition(packId);
        if (!definition.isActive()) {
            throw new RegraInvalidaException("Pacote inativo: " + packId);
        }

        List<CardAdminDTO> pool = selectedCards(definition, true);
        if (pool.isEmpty()) {
            throw new RegraInvalidaException("Pacote nao possui cartas ativas para abrir.");
        }

        PlayerProfileDTO profile = profileService.spendCoins(playerId, Math.max(0, definition.getCost()));
        List<CardAdminDTO> opened = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        int cardsPerPack = Math.max(1, definition.getCardsPerPack());
        for (int i = 0; i < cardsPerPack; i++) {
            CardAdminDTO card = draw(pool);
            opened.add(card);
            ids.add(card.getId());
        }
        collectionService.addCards(playerId, ids);

        PackOpening opening = openingRepository.save(PackOpening.builder()
                .id(UUID.randomUUID().toString())
                .playerId(playerId)
                .packId(definition.getId())
                .packName(definition.getNome())
                .cost(Math.max(0, definition.getCost()))
                .cardsJson(writeJson(ids))
                .createdAt(LocalDateTime.now())
                .build());
        return toDTO(opening, opened, profile.getCoins());
    }

    public List<PackOpeningDTO> history(String playerId) {
        Map<String, CardAdminDTO> cardsById = cardsById(cartaDataService.listarAdmin());
        return openingRepository.findTop10ByPlayerIdOrderByCreatedAtDesc(playerId).stream()
                .map(opening -> toDTO(opening, cardsFromIds(readIds(opening.getCardsJson()), cardsById), 0))
                .toList();
    }

    private PackDefinition toDefinition(PackAdminDTO dto, String idForcado) {
        boolean creating = idForcado == null || idForcado.isBlank();
        String id = creating
                ? (isBlank(dto.getId()) ? uniqueIdFor(dto.getNome()) : dto.getId().trim())
                : idForcado.trim();
        if (isBlank(id) || isBlank(dto.getNome())) {
            throw new RegraInvalidaException("Nome do pacote e obrigatorio.");
        }

        PackDefinition current = creating ? null : definitionRepository.findById(id).orElse(null);
        LocalDateTime now = LocalDateTime.now();
        List<String> cardIds = normalizeCardIds(dto.getCardIds());
        validateCardIds(cardIds);

        return PackDefinition.builder()
                .id(id)
                .nome(dto.getNome().trim())
                .descricao(trimToNull(dto.getDescricao()))
                .imageUrl(trimToNull(dto.getImageUrl()))
                .cost(Math.max(0, dto.getCost()))
                .cardsPerPack(dto.getCardsPerPack() <= 0 ? DEFAULT_CARDS_PER_PACK : dto.getCardsPerPack())
                .active(dto.isActive())
                .cardIdsJson(writeJson(cardIds))
                .createdAt(current == null || current.getCreatedAt() == null ? now : current.getCreatedAt())
                .updatedAt(now)
                .build();
    }

    private PackAdminDTO toAdminDTO(PackDefinition definition) {
        return toAdminDTO(definition, cardsById(cartaDataService.listarAdmin()));
    }

    private PackAdminDTO toAdminDTO(PackDefinition definition, Map<String, CardAdminDTO> cardsById) {
        List<String> cardIds = readIds(definition.getCardIdsJson());
        List<CollectionCardDTO> cards = selectedCards(definition, false, cardsById).stream()
                .map(card -> collectionService.toCollectionCard(card, 0))
                .toList();
        return PackAdminDTO.builder()
                .id(definition.getId())
                .nome(definition.getNome())
                .descricao(definition.getDescricao())
                .imageUrl(definition.getImageUrl())
                .cost(Math.max(0, definition.getCost()))
                .cardsPerPack(Math.max(1, definition.getCardsPerPack()))
                .active(definition.isActive())
                .cardIds(cardIds)
                .cards(cards)
                .cardCount(cardIds.size())
                .createdAt(definition.getCreatedAt())
                .updatedAt(definition.getUpdatedAt())
                .build();
    }

    private PackShopDTO toShopDTO(PackDefinition definition, Map<String, CardAdminDTO> cardsById) {
        List<CollectionCardDTO> cards = selectedCards(definition, true, cardsById).stream()
                .map(card -> collectionService.toCollectionCard(card, 0))
                .toList();
        return PackShopDTO.builder()
                .id(definition.getId())
                .nome(definition.getNome())
                .descricao(definition.getDescricao())
                .imageUrl(definition.getImageUrl())
                .cost(Math.max(0, definition.getCost()))
                .cardsPerPack(Math.max(1, definition.getCardsPerPack()))
                .cardCount(cards.size())
                .cards(cards)
                .build();
    }

    private CardAdminDTO draw(List<CardAdminDTO> pool) {
        CardRarity rarity = drawRarity();
        List<CardAdminDTO> filtered = pool.stream()
                .filter(card -> (card.getRarities() != null && card.getRarities().contains(rarity.name()))
                        || rarity.name().equals(card.getRarity()))
                .toList();
        List<CardAdminDTO> source = filtered.isEmpty() ? pool : filtered;
        CardAdminDTO card = source.get(random.nextInt(source.size()));
        return cartaDataService.comRaridade(card, filtered.isEmpty() ? card.getRarity() : rarity.name());
    }

    private CardRarity drawRarity() {
        int roll = random.nextInt(100) + 1;
        int cursor = 0;
        for (CardRarity rarity : CardRarity.values()) {
            cursor += ODDS.getOrDefault(rarity, 0);
            if (roll <= cursor) return rarity;
        }
        return CardRarity.COMUM;
    }

    private PackOpeningDTO toDTO(PackOpening opening, List<CardAdminDTO> cards, int coinsRemaining) {
        List<CollectionCardDTO> opened = cards.stream()
                .map(card -> collectionService.toCollectionCard(card, 1))
                .toList();
        return PackOpeningDTO.builder()
                .id(opening.getId())
                .playerId(opening.getPlayerId())
                .packId(opening.getPackId())
                .packName(opening.getPackName())
                .cost(opening.getCost())
                .coinsRemaining(coinsRemaining)
                .createdAt(opening.getCreatedAt())
                .cards(opened)
                .build();
    }

    private PackDefinition getDefinition(String id) {
        return definitionRepository.findById(id)
                .orElseThrow(() -> new RegraInvalidaException("Pacote nao encontrado: " + id));
    }

    private List<CardAdminDTO> selectedCards(PackDefinition definition, boolean activeOnly) {
        return selectedCards(definition, activeOnly, cardsById(cartaDataService.listarAdmin()));
    }

    private List<CardAdminDTO> selectedCards(PackDefinition definition,
                                             boolean activeOnly,
                                             Map<String, CardAdminDTO> cardsById) {
        return readIds(definition.getCardIdsJson()).stream()
                .map(cardsById::get)
                .filter(card -> card != null && (!activeOnly || card.isActive()))
                .toList();
    }

    private List<CardAdminDTO> cardsFromIds(List<String> ids, Map<String, CardAdminDTO> cardsById) {
        return ids.stream()
                .map(cardsById::get)
                .filter(card -> card != null)
                .toList();
    }

    private Map<String, CardAdminDTO> cardsById(List<CardAdminDTO> cards) {
        return cards.stream()
                .collect(Collectors.toMap(CardAdminDTO::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    private void validateCardIds(List<String> cardIds) {
        for (String cardId : cardIds) {
            if (cartaDataService.getById(cardId) == null) {
                throw new RegraInvalidaException("Carta nao encontrada no pacote: " + cardId);
            }
        }
    }

    private Map<String, Integer> odds() {
        Map<String, Integer> odds = new LinkedHashMap<>();
        for (CardRarity rarity : CardRarity.values()) {
            odds.put(rarity.name(), ODDS.getOrDefault(rarity, 0));
        }
        return odds;
    }

    private String writeJson(List<String> ids) {
        try {
            return objectMapper.writeValueAsString(ids == null ? List.of() : ids);
        } catch (JsonProcessingException ex) {
            throw new RegraInvalidaException("Falha ao registrar pacote: " + ex.getMessage());
        }
    }

    private List<String> readIds(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }

    private List<String> normalizeCardIds(List<String> cardIds) {
        if (cardIds == null) return List.of();
        return cardIds.stream()
                .filter(id -> !isBlank(id))
                .map(String::trim)
                .distinct()
                .toList();
    }

    private String uniqueIdFor(String nome) {
        String base = slug(nome);
        if (base == null || base.isBlank()) {
            throw new RegraInvalidaException("Nome do pacote e obrigatorio.");
        }
        String candidate = base;
        int suffix = 2;
        while (definitionRepository.existsById(candidate)) {
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
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
}
