package com.db.cmetal.be.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;

@Service
@RequiredArgsConstructor
public class ImageService {

    @Value("${app.images.path}")
    private String imagesPath;

    /**
     * Salva le immagini di un prodotto usando l'id del prodotto come nome
     * @param file file originale caricato dall'utente
     * @param prodottoId id del prodotto
     * @return path relativo dell'immagine principale
     */
    public String saveProductImage(MultipartFile file, Long prodottoId) throws IOException {
        if (file == null || file.isEmpty() || prodottoId == null) {
            return null;
        }

        Path uploadDir = Paths.get(imagesPath, "prodotti");
        Files.createDirectories(uploadDir);
        
        // Cancella eventuali immagini precedenti
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(uploadDir, prodottoId + "_*.jpeg")) {
            for (Path fileD : stream) {
                Files.deleteIfExists(fileD);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Salvo temporaneo
        Path tempFile = uploadDir.resolve("temp_" + System.currentTimeMillis() + ".tmp");
        file.transferTo(tempFile.toFile());

        // Leggo immagine
        BufferedImage original = ImageIO.read(tempFile.toFile());

        // Calcolo crop centrale per renderla quadrata
        int width = original.getWidth();
        int height = original.getHeight();
        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;

        // Percorsi definitivi JPEG con timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());
        Path originalJpeg = uploadDir.resolve(prodottoId + "_" + timestamp + ".jpeg");
        Path thumbJpeg    = uploadDir.resolve(prodottoId + "_" + timestamp + "_thumb.jpeg");
        Path mediumJpeg   = uploadDir.resolve(prodottoId + "_" + timestamp + "_medium.jpeg");

        // originale quadrata
        BufferedImage originalSquare = Thumbnails.of(original)
                                            .sourceRegion(x, y, size, size)
                                            .size(size, size)
                                            .outputQuality(1.0)
                                            .asBufferedImage();
        ImageIO.write(originalSquare, "jpeg", originalJpeg.toFile());

        // thumbnail 200x200
        BufferedImage thumb = Thumbnails.of(original)
                                        .sourceRegion(x, y, size, size)
                                        .size(200, 200)
                                        .outputQuality(0.8)
                                        .asBufferedImage();
        ImageIO.write(thumb, "jpeg", thumbJpeg.toFile());

        // medium 800x800
        BufferedImage medium = Thumbnails.of(original)
                                        .sourceRegion(x, y, size, size)
                                        .size(800, 800)
                                        .outputQuality(0.85)
                                        .asBufferedImage();
        ImageIO.write(medium, "jpeg", mediumJpeg.toFile());

        // elimina temporaneo
        Files.deleteIfExists(tempFile);

        // ritorna filename senza estensione
        String fileName = originalJpeg.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileName = fileName.substring(0, dotIndex); 
        }
        return fileName;
    }

}
