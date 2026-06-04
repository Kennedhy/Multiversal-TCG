package com.team.multiversaltcg.game.service;

import com.team.multiversaltcg.game.dto.EmoteAdminDTO;
import com.team.multiversaltcg.game.emotes.EmoteDefinition;
import com.team.multiversaltcg.game.emotes.EmoteDefinitionRepository;
import com.team.multiversaltcg.game.enums.PvpEmote;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class EmoteService {

    private final EmoteDefinitionRepository repository;

    public EmoteService(EmoteDefinitionRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void seedDefaults() {
        for (PvpEmote emote : PvpEmote.values()) {
            if (repository.existsById(emote.name())) continue;
            repository.save(EmoteDefinition.builder()
                    .id(emote.name())
                    .nome(emote.getDisplayName())
                    .gifUrl(emote.getGifUrl())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }
    }

    public List<EmoteAdminDTO> listarAdmin() {
        seedMissingDefaults();
        return repository.findAllByOrderByIdAsc().stream()
                .map(this::toDTO)
                .toList();
    }

    public EmoteAdminDTO buscarAdmin(String id) {
        return toDTO(buscarDefinition(id));
    }

    public EmoteAdminDTO salvar(EmoteAdminDTO dto, String idForcado) {
        if (dto == null) {
            throw new RegraInvalidaException("Dados do emote sao obrigatorios.");
        }

        String id = idForcado == null || idForcado.isBlank()
                ? idFrom(dto.getId(), dto.getNome())
                : cleanId(idForcado);
        if (id == null || id.isBlank()) {
            throw new RegraInvalidaException("ID ou nome do emote e obrigatorio.");
        }

        String gifUrl = dto.getGifUrl();
        if (gifUrl == null || gifUrl.isBlank()) {
            throw new RegraInvalidaException("URL do GIF e obrigatoria.");
        }

        EmoteDefinition atual = idForcado == null || idForcado.isBlank()
                ? repository.findById(id).orElse(null)
                : buscarDefinition(id);
        if (atual == null) {
            atual = EmoteDefinition.builder().id(id).build();
        }
        String nome = dto.getNome() == null || dto.getNome().isBlank()
                ? (atual.getNome() == null || atual.getNome().isBlank() ? id.replace("_", " ") : atual.getNome())
                : dto.getNome().trim();
        atual.setNome(nome);
        atual.setGifUrl(gifUrl.trim());
        atual.setUpdatedAt(LocalDateTime.now());
        return toDTO(repository.save(atual));
    }

    public EmoteAdminDTO atualizarGif(String id, String gifUrl) {
        if (gifUrl == null || gifUrl.isBlank()) {
            throw new RegraInvalidaException("URL do GIF e obrigatoria.");
        }
        EmoteDefinition definition = buscarDefinition(id);
        definition.setGifUrl(gifUrl.trim());
        definition.setUpdatedAt(LocalDateTime.now());
        return toDTO(repository.save(definition));
    }

    public EmoteAdminDTO salvarUploadGif(String id, String nome, String gifUrl) {
        if (gifUrl == null || gifUrl.isBlank()) {
            throw new RegraInvalidaException("URL do GIF e obrigatoria.");
        }
        String requestedId = cleanId(id);
        if (requestedId == null || requestedId.isBlank()) {
            throw new RegraInvalidaException("ID do emote e obrigatorio.");
        }

        EmoteDefinition definition = repository.findById(requestedId).orElse(null);
        if (definition == null) {
            String novoId = nome == null || nome.isBlank() ? requestedId : cleanId(nome);
            if (novoId == null || novoId.isBlank()) {
                throw new RegraInvalidaException("Nome do emote e obrigatorio.");
            }
            PvpEmote defaultEmote = PvpEmote.fromId(normalizeId(novoId));
            boolean defaultSemNomeCustomizado = (nome == null || nome.isBlank())
                    && defaultEmote != null
                    && defaultEmote.name().equals(novoId.toUpperCase(Locale.ROOT));
            definition = EmoteDefinition.builder()
                    .id(defaultSemNomeCustomizado
                            ? defaultEmote.name()
                            : novoId)
                    .nome(nome == null || nome.isBlank()
                            ? (defaultEmote == null ? novoId : defaultEmote.getDisplayName())
                            : nome.trim())
                    .build();
        } else if (nome != null && !nome.isBlank()) {
            definition.setNome(nome.trim());
        }

        definition.setGifUrl(gifUrl.trim());
        definition.setUpdatedAt(LocalDateTime.now());
        return toDTO(repository.save(definition));
    }

    public void excluir(String id) {
        EmoteDefinition definition = buscarDefinition(id);
        PvpEmote defaultEmote = PvpEmote.fromId(definition.getId());
        if (defaultEmote != null && defaultEmote.name().equals(definition.getId())) {
            throw new RegraInvalidaException("Emotes padrao nao podem ser excluidos.");
        }
        repository.deleteById(definition.getId());
    }

    public String gifUrlFor(String emoteId) {
        EmoteDefinition definition = buscarDefinition(emoteId);
        return definition.getGifUrl();
    }

    private EmoteDefinition buscarDefinition(String id) {
        String cleaned = cleanId(id);
        if (cleaned == null || cleaned.isBlank()) {
            throw new RegraInvalidaException("Emote invalido: " + id);
        }

        EmoteDefinition exact = repository.findById(cleaned).orElse(null);
        if (exact != null) {
            return exact;
        }

        String normalized = normalizeId(cleaned);
        PvpEmote defaultEmote = PvpEmote.fromId(normalized);
        if (defaultEmote != null && !repository.existsById(normalized)) {
            return repository.save(EmoteDefinition.builder()
                    .id(defaultEmote.name())
                    .nome(defaultEmote.getDisplayName())
                    .gifUrl(defaultEmote.getGifUrl())
                    .updatedAt(LocalDateTime.now())
                    .build());
        }
        if (defaultEmote != null) {
            return repository.findById(defaultEmote.name())
                    .orElseThrow(() -> new RegraInvalidaException("Emote invalido: " + id));
        }
        throw new RegraInvalidaException("Emote invalido: " + id);
    }

    private void seedMissingDefaults() {
        for (PvpEmote emote : PvpEmote.values()) {
            if (!repository.existsById(emote.name())) {
                repository.save(EmoteDefinition.builder()
                        .id(emote.name())
                        .nome(emote.getDisplayName())
                        .gifUrl(emote.getGifUrl())
                        .updatedAt(LocalDateTime.now())
                        .build());
            }
        }
    }

    private EmoteAdminDTO toDTO(EmoteDefinition definition) {
        return EmoteAdminDTO.builder()
                .id(definition.getId())
                .nome(definition.getNome())
                .gifUrl(definition.getGifUrl())
                .updatedAt(definition.getUpdatedAt())
                .build();
    }

    private String idFrom(String requestedId, String nome) {
        String base = nome == null || nome.isBlank() ? requestedId : nome;
        return cleanId(base);
    }

    private String cleanId(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isBlank() ? null : cleaned;
    }

    private String normalizeId(String value) {
        if (value == null) return null;
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return normalized.isBlank() ? null : normalized;
    }
}
