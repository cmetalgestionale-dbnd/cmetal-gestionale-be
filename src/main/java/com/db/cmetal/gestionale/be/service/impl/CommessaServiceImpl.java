package com.db.cmetal.gestionale.be.service.impl;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.db.cmetal.gestionale.be.dto.CommessaDto;
import com.db.cmetal.gestionale.be.entity.Allegato;
import com.db.cmetal.gestionale.be.entity.Commessa;
import com.db.cmetal.gestionale.be.entity.Utente;
import com.db.cmetal.gestionale.be.repository.AllegatoRepository;
import com.db.cmetal.gestionale.be.repository.CommessaRepository;
import com.db.cmetal.gestionale.be.service.CommessaService;
import com.db.cmetal.gestionale.be.service.SupabaseS3Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommessaServiceImpl implements CommessaService {

    private final CommessaRepository commessaRepository;
    private final AllegatoRepository allegatoRepository;
    private final SupabaseS3Service s3Service;
    private static final long MAX_BYTES = 512L * 1024L; // 0.5 MB

    public CommessaServiceImpl(CommessaRepository commessaRepository, 
                               SupabaseS3Service s3Service, 
                               AllegatoRepository allegatoRepository) {
        this.commessaRepository = commessaRepository;
        this.s3Service = s3Service;
        this.allegatoRepository = allegatoRepository;
    }

    @Override
    public Commessa saveCommessa(Commessa commessa, Utente user) {
        if (commessa.getId() == null) {
            commessa.setCreatedAt(LocalDateTime.now());
            commessa.setCreatedBy(user);
        }
        return commessaRepository.save(commessa);
    }

    @Override
    public List<Commessa> getAllCommesse() {
        return commessaRepository.findAll();
    }

    @Override
    public Optional<Commessa> getCommessaById(Long id) {
        return commessaRepository.findById(id);
    }

    @Override
    public Optional<Commessa> getCommessaByCodice(String codice) {
        return commessaRepository.findByCodice(codice);
    }

    @Override
    public Commessa updateCommessa(Long id, Commessa commessa) {
        return commessaRepository.findById(id)
                .map(existing -> {
                    existing.setCodice(commessa.getCodice());
                    existing.setDescrizione(commessa.getDescrizione());
                    existing.setPdfAllegato(commessa.getPdfAllegato());
                    existing.setIsDeleted(commessa.getIsDeleted());
                    existing.setCreatedBy(commessa.getCreatedBy());
                    existing.setCreatedAt(commessa.getCreatedAt());
                    return commessaRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Commessa non trovata con id: " + id));
    }

    @Override
    public void hardDeleteCommessa(Long id) {
        Commessa c = commessaRepository.findById(id).orElseThrow();
        if (c.getPdfAllegato() != null) {
            s3Service.deleteFile(c.getPdfAllegato().getStoragePath());
            allegatoRepository.delete(c.getPdfAllegato());
        }
        commessaRepository.delete(c);
    }

    @Override
    public void deleteCommessa(Long id) {
        Commessa c = commessaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commessa non trovata"));
        c.setIsDeleted(true);
        if (c.getPdfAllegato() != null) {
            Allegato a = c.getPdfAllegato();
            a.setIsDeleted(true);
            allegatoRepository.save(a);
        }
        commessaRepository.save(c);
    }

    @Override
    public void restoreCommessa(Long id) {
        Commessa c = commessaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commessa non trovata"));
        c.setIsDeleted(false);
        if (c.getPdfAllegato() != null) {
            Allegato a = c.getPdfAllegato();
            a.setIsDeleted(false);
            allegatoRepository.save(a);
        }
        commessaRepository.save(c);
    }

    // LOGICA spostata dal controller

    @Override
    public Commessa createCommessa(CommessaDto dto, MultipartFile file, Utente user) throws Exception {
        Commessa commessa = new Commessa();
        commessa.setCodice(dto.codice);
        commessa.setDescrizione(dto.descrizione);

        if (file != null && !file.isEmpty()) {
            Allegato allegato = uploadAndSaveAllegato(file, user);
            commessa.setPdfAllegato(allegato);
        }

        return saveCommessa(commessa, user);
    }

    @Override
    public Commessa updateCommessaWithFile(Long id, CommessaDto dto, MultipartFile file, Boolean removeFile, Utente user) throws Exception {
        Commessa existing = getCommessaById(id)
                .orElseThrow(() -> new RuntimeException("Commessa non trovata"));

        existing.setCodice(dto.codice);
        existing.setDescrizione(dto.descrizione);

        // Gestione rimozione allegato
        if (Boolean.TRUE.equals(removeFile) && existing.getPdfAllegato() != null) {
            Allegato a = existing.getPdfAllegato();
            a.setIsDeleted(true);
            allegatoRepository.save(a);
            existing.setPdfAllegato(null);
        }

        // Gestione sostituzione allegato
        if (file != null && !file.isEmpty()) {
            if (existing.getPdfAllegato() != null) {
                Allegato old = existing.getPdfAllegato();
                old.setIsDeleted(true);
                allegatoRepository.save(old);
            }
            Allegato newA = uploadAndSaveAllegato(file, user);
            existing.setPdfAllegato(newA);
        }

        return updateCommessa(id, existing);
    }

    private Allegato uploadAndSaveAllegato(MultipartFile file, Utente user) throws Exception {
        // Controllo tipo file
        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Il file deve essere un PDF");
        }

        // Controllo dimensione
        if (file.getSize() > 2_048_576) { // 2 MB
            throw new IllegalArgumentException("Il file non può superare 2 MB");
        }

        byte[] originalBytes = file.getBytes();
        byte[] toUploadBytes = originalBytes;

        // Se supera 0.5 MB, tentiamo la compressione iterativa
        if (originalBytes.length > MAX_BYTES) {
            byte[] compressed = compressPdfToLimit(originalBytes, MAX_BYTES);
            if (compressed == null) {
                throw new IllegalArgumentException("Il file PDF è troppo grande anche dopo la compressione (max 0.5 MB)");
            }
            toUploadBytes = compressed;
        }

        String path = "commesse/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        // Creiamo un MultipartFile in-memory con i bytes (non cambiamo l'interfaccia s3Service)
        MultipartFile multipartToUpload = new InMemoryMultipartFile(
                "file",
                file.getOriginalFilename(),
                file.getContentType(),
                toUploadBytes
        );

        s3Service.uploadFile(multipartToUpload, path);

        Allegato allegato = new Allegato();
        allegato.setNomeFile(file.getOriginalFilename());
        allegato.setTipoFile(file.getContentType());
        allegato.setStoragePath(path);
        allegato.setCreatedAt(LocalDateTime.now());
        allegato.setCreatedBy(user);
        allegato.setIsDeleted(false);

        return allegatoRepository.save(allegato);
    }

    /**
     * Tenta di comprimere il PDF ricodificando le immagini interne come JPEG con
     * qualità/scala decrescente. Ritorna byte[] se si rientra nel limite, altrimenti null.
     */
    private byte[] compressPdfToLimit(byte[] inputPdf, long maxBytes) {
        // Strategia: proviamo combinazioni di scale/quality
        float[] qualities = new float[] { 0.75f, 0.6f, 0.5f, 0.4f };
        double[] scales = new double[] { 1.0, 0.9, 0.8, 0.7 };

        for (double scale : scales) {
            for (float quality : qualities) {
                try (PDDocument doc = PDDocument.load(inputPdf)) {
                    boolean replacedAny = false;

                    for (PDPage page : doc.getPages()) {
                        PDResources resources = page.getResources();
                        if (resources == null) continue;

                        // Copiamo i nomi prima di iterare (per evitare ConcurrentModification)
                        for (COSName name : resources.getXObjectNames()) {
                            try {
                                PDXObject xobj = resources.getXObject(name);
                                if (xobj instanceof PDImageXObject) {
                                    PDImageXObject img = (PDImageXObject) xobj;
                                    BufferedImage bim = img.getImage();

                                    // Calcola nuova dimensione in base alla scala
                                    int newW = Math.max(1, (int) Math.round(bim.getWidth() * scale));
                                    int newH = Math.max(1, (int) Math.round(bim.getHeight() * scale));

                                    // Ridimensionamento
                                    BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
                                    Graphics2D g = resized.createGraphics();
                                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                                    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                                    g.drawImage(bim, 0, 0, newW, newH, null);
                                    g.dispose();

                                    // Ricodifica come JPEG con la qualità scelta
                                    PDImageXObject jpg = JPEGFactory.createFromImage(doc, resized, quality);

                                    // Sostituisci l'immagine nelle risorse
                                    resources.put(name, jpg);
                                    replacedAny = true;
                                }
                            } catch (Exception e) {
                                // Non blocchiamo l'intero processo per un'immagine; loggare e proseguire
                                log.warn("Errore comprimendo immagine in pagina: {}", e.getMessage());
                            }
                        }
                    }

                    // Se non abbiamo trovato immagini, possiamo comunque salvare con compressione stream
                    // (PDFBox applicherà compressione su flussi durante il salvataggio).
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    doc.save(baos);
                    byte[] out = baos.toByteArray();

                    log.info("Tentativo compressione: scale={}, quality={}, size={} KB, replacedAny={}",
                            scale, quality, out.length / 1024, replacedAny);

                    if (out.length <= maxBytes) {
                        return out;
                    }
                    // altrimenti continua con altre combinazioni
                } catch (IOException ioe) {
                    log.warn("Errore durante compressione tentativo scale={}, quality={}: {}", scale, quality, ioe.getMessage());
                    // continua con il prossimo tentativo
                }
            }
        }

        // Se qui, tutti i tentativi non sono riusciti -> ritorna null (caller solleverà eccezione)
        return null;
    }

    /**
     * Implementazione minimale di MultipartFile in memoria (per chiamare uploadFile esistente).
     */
    private static class InMemoryMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        public InMemoryMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content == null ? new byte[0] : content;
        }

        @Override public String getName() { return name; }
        @Override public String getOriginalFilename() { return originalFilename; }
        @Override public String getContentType() { return contentType; }
        @Override public boolean isEmpty() { return content.length == 0; }
        @Override public long getSize() { return content.length; }
        @Override public byte[] getBytes() throws IOException { return content; }
        @Override public java.io.InputStream getInputStream() throws IOException { return new ByteArrayInputStream(content); }
        @Override public void transferTo(File dest) throws IOException { java.nio.file.Files.write(dest.toPath(), content); }
    }

    @Override
    public Optional<String> getAllegatoUrl(Long id) {
        return getCommessaById(id)
                .filter(c -> c.getPdfAllegato() != null)
                .map(c -> s3Service.getPublicUrl(c.getPdfAllegato().getStoragePath()));
    }

    @Override
    public Optional<ResponseEntity<byte[]>> getAllegatoFile(Long id) throws Exception {
        return getCommessaById(id)
                .filter(c -> c.getPdfAllegato() != null)
                .map(c -> {
                    Allegato allegato = c.getPdfAllegato();
                    try {
                        byte[] fileBytes = s3Service.downloadFile(allegato.getStoragePath());
                        return ResponseEntity.ok()
                                .header("Content-Disposition", "inline; filename=\"" + allegato.getNomeFile() + "\"")
                                .contentType(MediaType.parseMediaType(allegato.getTipoFile()))
                                .body(fileBytes);
                    } catch (Exception e) {
                        throw new RuntimeException("Errore durante il download del file", e);
                    }
                });
    }
}
