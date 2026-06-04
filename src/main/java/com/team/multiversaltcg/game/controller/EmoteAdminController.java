package com.team.multiversaltcg.game.controller;

import com.team.multiversaltcg.game.dto.EmoteAdminDTO;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.service.EmoteService;
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
import java.util.UUID;

@RestController
public class EmoteAdminController {

    private static final long MAX_GIF_BYTES = 5 * 1024 * 1024;

    private final EmoteService emoteService;
    private final Path uploadRoot = Path.of("data", "uploads", "emotes").toAbsolutePath().normalize();

    public EmoteAdminController(EmoteService emoteService) {
        this.emoteService = emoteService;
    }

    @GetMapping("/api/emotes")
    public List<EmoteAdminDTO> listar() {
        return emoteService.listarAdmin();
    }

    @GetMapping("/api/emotes/{id}")
    public EmoteAdminDTO buscar(@PathVariable String id) {
        return emoteService.buscarAdmin(id);
    }

    @PostMapping("/api/emotes")
    public EmoteAdminDTO criar(@RequestBody EmoteAdminDTO dto) {
        return emoteService.salvar(dto, null);
    }

    @PutMapping("/api/emotes/{id}")
    public EmoteAdminDTO atualizar(@PathVariable String id, @RequestBody EmoteAdminDTO dto) {
        return emoteService.salvar(dto, id);
    }

    @DeleteMapping("/api/emotes/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        emoteService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/emotes/{id}/gif")
    public EmoteAdminDTO upload(@PathVariable String id,
                                @RequestParam(required = false) String nome,
                                @RequestPart("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RegraInvalidaException("Arquivo GIF e obrigatorio.");
        }
        if (file.getSize() > MAX_GIF_BYTES) {
            throw new RegraInvalidaException("GIF deve ter no maximo 5 MB.");
        }
        String extension = extension(file.getOriginalFilename());
        if (!"gif".equals(extension)) {
            throw new RegraInvalidaException("Formato invalido. Use GIF.");
        }

        Files.createDirectories(uploadRoot);
        String safeId = id.replaceAll("[^a-zA-Z0-9_-]", "_");
        String filename = safeId + "-" + UUID.randomUUID() + ".gif";
        Path target = uploadRoot.resolve(filename).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new RegraInvalidaException("Caminho de upload invalido.");
        }
        file.transferTo(target);

        return emoteService.salvarUploadGif(id, nome, "/uploads/emotes/" + filename);
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
