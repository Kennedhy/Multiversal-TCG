package com.team.multiversaltcg.game.controller;

import com.team.multiversaltcg.game.dto.CardAdminDTO;
import com.team.multiversaltcg.game.dto.DeckDefaultDTO;
import com.team.multiversaltcg.game.dto.ImageUploadDTO;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.service.CartaDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
public class CardAdminController {

    private static final long MAX_IMAGE_BYTES = 2 * 1024 * 1024;
    private static final Set<String> EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");

    private final CartaDataService cartaDataService;
    private final Path uploadRoot = Path.of("data", "uploads", "cards").toAbsolutePath().normalize();

    public CardAdminController(CartaDataService cartaDataService) {
        this.cartaDataService = cartaDataService;
    }

    @GetMapping("/api/cards")
    public List<CardAdminDTO> listar() {
        return cartaDataService.listarAdmin();
    }

    @GetMapping("/api/cards/options")
    public Map<String, Object> options() {
        return cartaDataService.getOptions();
    }

    @GetMapping("/api/cards/{id}")
    public CardAdminDTO buscar(@PathVariable String id) {
        return cartaDataService.buscarAdmin(id);
    }

    @PostMapping("/api/cards")
    public CardAdminDTO criar(@RequestBody CardAdminDTO dto) {
        return cartaDataService.salvar(dto, null);
    }

    @PutMapping("/api/cards/{id}")
    public CardAdminDTO editar(@PathVariable String id, @RequestBody CardAdminDTO dto) {
        return cartaDataService.salvar(dto, id);
    }

    @DeleteMapping("/api/cards/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        cartaDataService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/cards/{id}/image")
    public ImageUploadDTO upload(@PathVariable String id,
                                  @RequestParam(required = false) String rarity,
                                  @RequestPart("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RegraInvalidaException("Arquivo de imagem e obrigatorio.");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new RegraInvalidaException("Imagem deve ter no maximo 2 MB.");
        }
        String extension = extension(file.getOriginalFilename());
        if (!EXTENSIONS.contains(extension)) {
            throw new RegraInvalidaException("Formato de imagem invalido. Use PNG, JPG ou WebP.");
        }

        Files.createDirectories(uploadRoot);
        String safeId = id.replaceAll("[^a-zA-Z0-9_-]", "_");
        String filename = safeId + "-" + UUID.randomUUID() + "." + extension;
        Path target = uploadRoot.resolve(filename).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new RegraInvalidaException("Caminho de upload invalido.");
        }
        file.transferTo(target);

        String imageUrl = "/uploads/cards/" + filename;
        cartaDataService.atualizarImagem(id, imageUrl, rarity);
        return new ImageUploadDTO(imageUrl);
    }

    @GetMapping("/api/deck/default")
    public DeckDefaultDTO deck() {
        return cartaDataService.getDeckDefaultDTO();
    }

    @PutMapping("/api/deck/default")
    public DeckDefaultDTO salvarDeck(@RequestBody DeckDefaultDTO dto) {
        return cartaDataService.salvarDeckDefault(dto);
    }

    private String extension(String filename) {
        String ext = StringUtils.getFilenameExtension(filename);
        return ext == null ? "" : ext.toLowerCase(Locale.ROOT);
    }

    @ExceptionHandler(RegraInvalidaException.class)
    public ResponseEntity<Map<String, String>> regraInvalida(RegraInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("erro", ex.getMessage()));
    }
}
