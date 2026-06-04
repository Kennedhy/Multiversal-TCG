package com.team.multiversaltcg.game.service;

import com.team.multiversaltcg.game.collections.PlayerCardCollection;
import com.team.multiversaltcg.game.collections.PlayerCardCollectionRepository;
import com.team.multiversaltcg.game.dto.CardAdminDTO;
import com.team.multiversaltcg.game.dto.CollectionCardDTO;
import com.team.multiversaltcg.game.dto.CollectionDTO;
import com.team.multiversaltcg.game.dto.DeckEntryDTO;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlayerCollectionService {

    private final PlayerCardCollectionRepository repository;
    private final CartaDataService cartaDataService;

    public PlayerCollectionService(PlayerCardCollectionRepository repository, CartaDataService cartaDataService) {
        this.repository = repository;
        this.cartaDataService = cartaDataService;
    }

    public CollectionDTO getCollection(String playerId) {
        Map<String, Integer> owned = ownedMap(playerId);
        List<CollectionCardDTO> cards = cartaDataService.listarAdmin().stream()
                .map(card -> toCollectionCard(card, owned.getOrDefault(card.getId(), 0)))
                .toList();
        return CollectionDTO.builder()
                .playerId(playerId)
                .totalOwned(owned.values().stream().mapToInt(Integer::intValue).sum())
                .uniqueOwned((int) owned.values().stream().filter(copies -> copies > 0).count())
                .cards(cards)
                .build();
    }

    @Transactional
    public void addCards(String playerId, List<String> cardIds) {
        Map<String, Integer> copiesByCard = new HashMap<>();
        for (String cardId : cardIds == null ? List.<String>of() : cardIds) {
            copiesByCard.merge(cardId, 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> entry : copiesByCard.entrySet()) {
            addCard(playerId, entry.getKey(), entry.getValue());
        }
    }

    @Transactional
    public void addCard(String playerId, String cardId, int copies) {
        if (cartaDataService.getById(cardId) == null) {
            throw new RegraInvalidaException("Carta nao encontrada: " + cardId);
        }
        PlayerCardCollection entry = repository.findByPlayerIdAndCardId(playerId, cardId)
                .orElseGet(() -> PlayerCardCollection.builder()
                        .id(PlayerCardCollection.idFor(playerId, cardId))
                        .playerId(playerId)
                        .cardId(cardId)
                        .copies(0)
                        .build());
        entry.setCopies(entry.getCopies() + Math.max(0, copies));
        repository.save(entry);
    }

    public int copiesOf(String playerId, String cardId) {
        return repository.findByPlayerIdAndCardId(playerId, cardId)
                .map(PlayerCardCollection::getCopies)
                .orElse(0);
    }

    public void validarDeckPossuido(String playerId, List<DeckEntryDTO> cards) {
        Map<String, Integer> requested = new HashMap<>();
        for (DeckEntryDTO entry : cards == null ? List.<DeckEntryDTO>of() : cards) {
            if (entry.getDeckCopies() <= 0) continue;
            requested.merge(entry.getId(), entry.getDeckCopies(), Integer::sum);
        }
        Map<String, Integer> owned = ownedMap(playerId);
        for (Map.Entry<String, Integer> entry : requested.entrySet()) {
            int ownedCopies = owned.getOrDefault(entry.getKey(), 0);
            if (entry.getValue() > ownedCopies) {
                throw new RegraInvalidaException("Deck usa " + entry.getValue()
                        + " copia(s) de " + entry.getKey() + ", mas a colecao possui " + ownedCopies + ".");
            }
        }
    }

    private Map<String, Integer> ownedMap(String playerId) {
        Map<String, Integer> owned = new HashMap<>();
        for (PlayerCardCollection entry : repository.findByPlayerIdOrderByCardIdAsc(playerId)) {
            owned.put(entry.getCardId(), entry.getCopies());
        }
        return owned;
    }

    public CollectionCardDTO toCollectionCard(CardAdminDTO card, int copies) {
        return CollectionCardDTO.builder()
                .id(card.getId())
                .nome(card.getNome())
                .cardType(card.getCardType())
                .rarity(card.getRarity())
                .rarities(card.getRarities())
                .rarityImageUrls(card.getRarityImageUrls())
                .tipo(card.getTipo())
                .universo(card.getUniverso())
                .imageUrl(card.getImageUrl())
                .copies(copies)
                .owned(copies > 0)
                .build();
    }
}
