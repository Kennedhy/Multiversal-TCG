package com.team.multiversaltcg.game.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team.multiversaltcg.game.decks.PlayerDeck;
import com.team.multiversaltcg.game.decks.PlayerDeckRepository;
import com.team.multiversaltcg.game.dto.DeckDefaultDTO;
import com.team.multiversaltcg.game.dto.DeckEntryDTO;
import com.team.multiversaltcg.game.dto.PlayerDeckDTO;
import com.team.multiversaltcg.game.dto.PlayerDeckSummaryDTO;
import com.team.multiversaltcg.game.model.Carta;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PlayerDeckService {

    private final PlayerDeckRepository repository;
    private final CartaDataService cartaDataService;
    private final PlayerCollectionService collectionService;
    private final ObjectMapper objectMapper;

    public PlayerDeckService(PlayerDeckRepository repository,
                             CartaDataService cartaDataService,
                             PlayerCollectionService collectionService,
                             ObjectMapper objectMapper) {
        this.repository = repository;
        this.cartaDataService = cartaDataService;
        this.collectionService = collectionService;
        this.objectMapper = objectMapper;
    }

    public List<PlayerDeckSummaryDTO> listar(String playerId) {
        String owner = normalizePlayer(playerId);
        return repository.findByPlayerIdOrderByUpdatedAtDesc(owner).stream()
                .map(deck -> PlayerDeckSummaryDTO.builder()
                        .id(deck.getId())
                        .playerId(deck.getPlayerId())
                        .name(deck.getName())
                        .total(readCards(deck).stream().mapToInt(DeckEntryDTO::getDeckCopies).sum())
                        .build())
                .toList();
    }

    public PlayerDeckDTO buscar(String playerId, String deckId) {
        PlayerDeck deck = getOwned(playerId, deckId);
        return toDTO(deck);
    }

    public PlayerDeckDTO criar(String playerId, PlayerDeckDTO dto) {
        String owner = normalizePlayer(playerId);
        List<DeckEntryDTO> cards = normalizedCards(dto.getCards());
        collectionService.validarDeckPossuido(owner, cards);
        String id = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        PlayerDeck deck = PlayerDeck.builder()
                .id(id)
                .playerId(owner)
                .name(normalizeName(dto.getName()))
                .cardsJson(writeCards(cards))
                .createdAt(now)
                .updatedAt(now)
                .build();
        repository.save(deck);
        return toDTO(deck);
    }

    public PlayerDeckDTO atualizar(String playerId, String deckId, PlayerDeckDTO dto) {
        PlayerDeck deck = getOwned(playerId, deckId);
        String owner = normalizePlayer(playerId);
        List<DeckEntryDTO> cards = normalizedCards(dto.getCards());
        collectionService.validarDeckPossuido(owner, cards);
        deck.setName(normalizeName(dto.getName()));
        deck.setCardsJson(writeCards(cards));
        deck.setUpdatedAt(LocalDateTime.now());
        repository.save(deck);
        return toDTO(deck);
    }

    public void excluir(String playerId, String deckId) {
        PlayerDeck deck = getOwned(playerId, deckId);
        repository.delete(deck);
    }

    public PlayerDeckDTO copiarPadrao(String playerId, String name) {
        DeckDefaultDTO padrao = cartaDataService.getDeckDefaultDTO();
        return criar(playerId, PlayerDeckDTO.builder()
                .name(name == null || name.isBlank() ? "Deck Padrao" : name)
                .cards(padrao.getCards())
                .build());
    }

    public List<Carta> montarDeck(String playerId, String deckId) {
        PlayerDeck deck = getOwned(playerId, deckId);
        List<DeckEntryDTO> cards = readCards(deck);
        collectionService.validarDeckPossuido(deck.getPlayerId(), cards);
        return cartaDataService.montarDeck(cards);
    }

    private PlayerDeck getOwned(String playerId, String deckId) {
        String owner = normalizePlayer(playerId);
        PlayerDeck deck = repository.findById(deckId)
                .orElseThrow(() -> new RegraInvalidaException("Deck nao encontrado: " + deckId));
        if (!owner.equals(deck.getPlayerId())) {
            throw new RegraInvalidaException("Deck nao pertence ao jogador informado.");
        }
        return deck;
    }

    private PlayerDeckDTO toDTO(PlayerDeck deck) {
        List<DeckEntryDTO> cards = readCards(deck);
        return PlayerDeckDTO.builder()
                .id(deck.getId())
                .playerId(deck.getPlayerId())
                .name(deck.getName())
                .total(cards.stream().mapToInt(DeckEntryDTO::getDeckCopies).sum())
                .cards(cards)
                .build();
    }

    private List<DeckEntryDTO> normalizedCards(List<DeckEntryDTO> cards) {
        List<DeckEntryDTO> normalized = cards == null ? List.of() : cards.stream()
                .map(entry -> DeckEntryDTO.builder()
                        .id(entry.getId())
                        .active(entry.getDeckCopies() > 0)
                        .deckCopies(Math.max(0, entry.getDeckCopies()))
                        .build())
                .toList();
        cartaDataService.montarDeck(normalized);
        return normalized;
    }

    private String normalizePlayer(String playerId) {
        if (playerId == null || playerId.isBlank()) return "local";
        return playerId.trim().toLowerCase();
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) return "Novo Deck";
        return name.trim();
    }

    private String writeCards(List<DeckEntryDTO> cards) {
        try {
            return objectMapper.writeValueAsString(cards);
        } catch (JsonProcessingException ex) {
            throw new RegraInvalidaException("Falha ao salvar deck: " + ex.getMessage());
        }
    }

    private List<DeckEntryDTO> readCards(PlayerDeck deck) {
        try {
            return objectMapper.readValue(deck.getCardsJson(), new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            return List.of();
        }
    }
}
