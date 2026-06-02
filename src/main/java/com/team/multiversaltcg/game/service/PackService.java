package com.team.multiversaltcg.game.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.multiversaltcg.game.dto.CardAdminDTO;
import com.team.multiversaltcg.game.dto.CollectionCardDTO;
import com.team.multiversaltcg.game.dto.PackOpeningDTO;
import com.team.multiversaltcg.game.dto.PlayerProfileDTO;
import com.team.multiversaltcg.game.dto.ShopDTO;
import com.team.multiversaltcg.game.enums.CardRarity;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.packs.PackOpening;
import com.team.multiversaltcg.game.packs.PackOpeningRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PackService {

    public static final int PACK_COST = 100;
    public static final int CARDS_PER_PACK = 5;

    private static final Map<CardRarity, Integer> ODDS = Map.of(
            CardRarity.COMUM, 55,
            CardRarity.INCOMUM, 25,
            CardRarity.RARO, 13,
            CardRarity.EPICO, 5,
            CardRarity.LENDARIO, 2
    );

    private final PackOpeningRepository repository;
    private final CartaDataService cartaDataService;
    private final PlayerCollectionService collectionService;
    private final PlayerProfileService profileService;
    private final ObjectMapper objectMapper;
    private final SecureRandom random = new SecureRandom();

    public PackService(PackOpeningRepository repository,
                       CartaDataService cartaDataService,
                       PlayerCollectionService collectionService,
                       PlayerProfileService profileService,
                       ObjectMapper objectMapper) {
        this.repository = repository;
        this.cartaDataService = cartaDataService;
        this.collectionService = collectionService;
        this.profileService = profileService;
        this.objectMapper = objectMapper;
    }

    public ShopDTO getShop(String playerId) {
        PlayerProfileDTO profile = profileService.ensureProfile(playerId);
        Map<String, Integer> odds = new LinkedHashMap<>();
        for (CardRarity rarity : CardRarity.values()) {
            odds.put(rarity.name(), ODDS.getOrDefault(rarity, 0));
        }
        return ShopDTO.builder()
                .playerId(playerId)
                .coins(profile.getCoins())
                .packCost(PACK_COST)
                .cardsPerPack(CARDS_PER_PACK)
                .odds(odds)
                .build();
    }

    public PackOpeningDTO buyPack(String playerId) {
        PlayerProfileDTO profile = profileService.spendCoins(playerId, PACK_COST);
        List<CardAdminDTO> pool = cartaDataService.listarAtivasAdmin();
        if (pool.isEmpty()) {
            throw new RegraInvalidaException("Nao ha cartas ativas para abrir pacote.");
        }

        List<CardAdminDTO> opened = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < CARDS_PER_PACK; i++) {
            CardAdminDTO card = draw(pool);
            opened.add(card);
            ids.add(card.getId());
        }
        collectionService.addCards(playerId, ids);

        PackOpening opening = repository.save(PackOpening.builder()
                .id(UUID.randomUUID().toString())
                .playerId(playerId)
                .cost(PACK_COST)
                .cardsJson(writeJson(ids))
                .createdAt(LocalDateTime.now())
                .build());
        return toDTO(opening, opened, profile.getCoins());
    }

    public List<PackOpeningDTO> history(String playerId) {
        return repository.findTop10ByPlayerIdOrderByCreatedAtDesc(playerId).stream()
                .map(opening -> toDTO(opening, cardsFromIds(readIds(opening.getCardsJson())), 0))
                .toList();
    }

    private CardAdminDTO draw(List<CardAdminDTO> pool) {
        CardRarity rarity = drawRarity();
        List<CardAdminDTO> filtered = pool.stream()
                .filter(card -> card.getRarities() != null && card.getRarities().contains(rarity.name())
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
                .cost(opening.getCost())
                .coinsRemaining(coinsRemaining)
                .createdAt(opening.getCreatedAt())
                .cards(opened)
                .build();
    }

    private List<CardAdminDTO> cardsFromIds(List<String> ids) {
        return ids.stream()
                .map(cartaDataService::buscarAdmin)
                .toList();
    }

    private String writeJson(List<String> ids) {
        try {
            return objectMapper.writeValueAsString(ids);
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
}
