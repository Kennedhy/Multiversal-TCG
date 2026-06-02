package com.team.multiversaltcg.game.service;

import com.team.multiversaltcg.game.dto.PlayerProfileDTO;
import com.team.multiversaltcg.game.players.PlayerProfile;
import com.team.multiversaltcg.game.players.PlayerProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class PlayerProfileService {

    public static final int INITIAL_COINS = 1000;

    private final PlayerProfileRepository repository;

    public PlayerProfileService(PlayerProfileRepository repository) {
        this.repository = repository;
    }

    public PlayerProfileDTO ensureProfile(String playerId) {
        PlayerProfile profile = repository.findById(playerId)
                .orElseGet(() -> repository.save(PlayerProfile.builder()
                        .playerId(playerId)
                        .build()));
        if (!profile.isInitialBonusGranted()) {
            profile.setCoins(profile.getCoins() + INITIAL_COINS);
            profile.setInitialBonusGranted(true);
            profile = repository.save(profile);
        }
        return PlayerProfileDTO.from(profile);
    }

    public PlayerProfile getProfileEntity(String playerId) {
        ensureProfile(playerId);
        return repository.findById(playerId).orElseThrow();
    }

    public PlayerProfileDTO spendCoins(String playerId, int amount) {
        PlayerProfile profile = getProfileEntity(playerId);
        if (profile.getCoins() < amount) {
            throw new com.team.multiversaltcg.game.model.RegraInvalidaException("Moedas insuficientes.");
        }
        profile.setCoins(profile.getCoins() - amount);
        return PlayerProfileDTO.from(repository.save(profile));
    }
}
