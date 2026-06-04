package com.team.multiversaltcg.game.service;

import com.team.multiversaltcg.game.model.RegraInvalidaException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class CardImageUploadService {

    private static final long MAX_IMAGE_BYTES = 2 * 1024 * 1024;
    private static final Set<String> EXTENSIONS = Set.of("png", "jpg", "jpeg", "webp");
    private static final String OUTPUT_EXTENSION = "webp";

    private final Path uploadRoot = Path.of("data", "uploads", "cards").toAbsolutePath().normalize();

    public String salvarComoWebp(String cardId, MultipartFile file) throws IOException {
        validarArquivo(file);

        BufferedImage image;
        try (InputStream input = file.getInputStream()) {
            image = ImageIO.read(input);
        }
        if (image == null) {
            throw new RegraInvalidaException("Imagem invalida ou corrompida.");
        }

        Files.createDirectories(uploadRoot);
        String safeId = cardId.replaceAll("[^a-zA-Z0-9_-]", "_");
        String filename = safeId + "-" + UUID.randomUUID() + "." + OUTPUT_EXTENSION;
        Path target = uploadRoot.resolve(filename).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new RegraInvalidaException("Caminho de upload invalido.");
        }

        try (OutputStream output = Files.newOutputStream(target)) {
            boolean written = ImageIO.write(image, OUTPUT_EXTENSION, output);
            if (!written) {
                throw new RegraInvalidaException("Nao foi possivel converter a imagem para WebP.");
            }
        } catch (IOException | RuntimeException ex) {
            Files.deleteIfExists(target);
            throw ex;
        }

        return "/uploads/cards/" + filename;
    }

    private void validarArquivo(MultipartFile file) {
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
    }

    private String extension(String filename) {
        String ext = StringUtils.getFilenameExtension(filename);
        return ext == null ? "" : ext.toLowerCase(Locale.ROOT);
    }
}
