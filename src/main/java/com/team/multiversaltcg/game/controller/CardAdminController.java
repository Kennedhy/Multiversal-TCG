package com.team.multiversaltcg.game.controller;

import com.team.multiversaltcg.game.dto.CardAdminDTO;
import com.team.multiversaltcg.game.dto.DeckDefaultDTO;
import com.team.multiversaltcg.game.dto.ImageUploadDTO;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.service.CardImageUploadService;
import com.team.multiversaltcg.game.service.CartaDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.util.List;
import java.util.Map;

@RestController
public class CardAdminController {

    private final CartaDataService cartaDataService;
    private final CardImageUploadService cardImageUploadService;

    public CardAdminController(CartaDataService cartaDataService,
                               CardImageUploadService cardImageUploadService) {
        this.cartaDataService = cartaDataService;
        this.cardImageUploadService = cardImageUploadService;
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
        String imageUrl = cardImageUploadService.salvarComoWebp(id, file);
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
