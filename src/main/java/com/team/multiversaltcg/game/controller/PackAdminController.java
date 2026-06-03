package com.team.multiversaltcg.game.controller;

import com.team.multiversaltcg.game.dto.PackAdminDTO;
import com.team.multiversaltcg.game.model.RegraInvalidaException;
import com.team.multiversaltcg.game.service.PackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class PackAdminController {

    private final PackService packService;

    public PackAdminController(PackService packService) {
        this.packService = packService;
    }

    @GetMapping("/api/packs")
    public List<PackAdminDTO> listar() {
        return packService.listarAdmin();
    }

    @GetMapping("/api/packs/{id}")
    public PackAdminDTO buscar(@PathVariable String id) {
        return packService.buscarAdmin(id);
    }

    @PostMapping("/api/packs")
    public PackAdminDTO criar(@RequestBody PackAdminDTO dto) {
        return packService.salvar(dto, null);
    }

    @PutMapping("/api/packs/{id}")
    public PackAdminDTO editar(@PathVariable String id, @RequestBody PackAdminDTO dto) {
        return packService.salvar(dto, id);
    }

    @DeleteMapping("/api/packs/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        packService.excluir(id);
        return ResponseEntity.noContent().build();
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
