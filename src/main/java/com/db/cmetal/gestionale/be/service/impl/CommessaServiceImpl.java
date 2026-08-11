package com.db.cmetal.gestionale.be.service.impl;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import com.db.cmetal.gestionale.be.service.WebSocketService;
import com.db.cmetal.gestionale.be.utils.Constants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CommessaServiceImpl implements CommessaService {

    private final CommessaRepository commessaRepository;
    private final AllegatoRepository allegatoRepository;
    private final SupabaseS3Service s3Service;
    private final WebSocketService wsService;
    private static final long MAX_BYTES = 1_048_576L; // 1 MB
    private static final int MAX_ALLEGATI_COMMESSA = 10;

    public CommessaServiceImpl(CommessaRepository commessaRepository, 
                               SupabaseS3Service s3Service, 
                               AllegatoRepository allegatoRepository, 
                               WebSocketService wsService) {
        this.commessaRepository = commessaRepository;
        this.s3Service = s3Service;
        this.allegatoRepository = allegatoRepository;
        this.wsService = wsService;
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
    public List<Commessa> getAllExistingCommesse() {
        return commessaRepository.findByIsDeletedFalse();
    }
    
    @Override
    public List<Commessa> getAllDeletedCommesse() {
        return commessaRepository.findByIsDeletedTrue();
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
	                    existing.setAllegati(commessa.getAllegati());
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
        Set<Long> deletedAllegatoIds = new HashSet<>();
        if (c.getPdfAllegato() != null) {
            s3Service.deleteFile(c.getPdfAllegato().getStoragePath());
            allegatoRepository.delete(c.getPdfAllegato());
            deletedAllegatoIds.add(c.getPdfAllegato().getId());
        }
        for (Allegato allegato : c.getAllegati()) {
            if (deletedAllegatoIds.add(allegato.getId())) {
                s3Service.deleteFile(allegato.getStoragePath());
                allegatoRepository.delete(allegato);
            }
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
	        for (Allegato a : c.getAllegati()) {
	            a.setIsDeleted(true);
	            allegatoRepository.save(a);
	        }
	        commessaRepository.save(c);
	        wsService.broadcast(Constants.MSG_REFRESH, null);
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
	        for (Allegato a : c.getAllegati()) {
	            a.setIsDeleted(false);
	            allegatoRepository.save(a);
	        }
	        commessaRepository.save(c);
	        wsService.broadcast(Constants.MSG_REFRESH, null);
	    }

    // LOGICA spostata dal controller

    @Override
    public Commessa createCommessa(CommessaDto dto, List<MultipartFile> files, Utente user) throws Exception {
        Commessa commessa = new Commessa();
        commessa.setCodice(dto.codice);
        commessa.setDescrizione(dto.descrizione);

        List<MultipartFile> validFiles = normalizeFiles(files);
        if (validFiles.size() > MAX_ALLEGATI_COMMESSA) {
            throw new IllegalArgumentException("Puoi allegare al massimo 10 PDF per commessa");
        }
        List<Allegato> allegati = new ArrayList<>();
        for (MultipartFile file : validFiles) {
            allegati.add(uploadAndSaveAllegato(file, user));
        }
        commessa.setAllegati(allegati);
        if (!allegati.isEmpty()) {
            commessa.setPdfAllegato(allegati.get(0));
        }

        Commessa saved = saveCommessa(commessa, user);
        wsService.broadcast(Constants.MSG_REFRESH, null);
        return saved;
    }

    @Override
    public Commessa updateCommessaWithFile(Long id, CommessaDto dto, List<MultipartFile> files, Boolean removeFile,
            List<Long> removeAllegatoIds, Utente user) throws Exception {
        Commessa existing = getCommessaById(id)
                .orElseThrow(() -> new RuntimeException("Commessa non trovata"));

        existing.setCodice(dto.codice);
        existing.setDescrizione(dto.descrizione);
        if (existing.getAllegati() == null) {
            existing.setAllegati(new ArrayList<>());
        }

        if (existing.getAllegati().isEmpty() && existing.getPdfAllegato() != null) {
            existing.getAllegati().add(existing.getPdfAllegato());
        }

        if (Boolean.TRUE.equals(removeFile)) {
            removeAllegati(existing, existing.getAllegati().stream().map(Allegato::getId).toList());
        }
        if (removeAllegatoIds != null && !removeAllegatoIds.isEmpty()) {
            removeAllegati(existing, removeAllegatoIds);
        }

        List<MultipartFile> validFiles = normalizeFiles(files);
        if (existing.getAllegati().size() + validFiles.size() > MAX_ALLEGATI_COMMESSA) {
            throw new IllegalArgumentException("Puoi allegare al massimo 10 PDF per commessa");
        }
        for (MultipartFile file : validFiles) {
            existing.getAllegati().add(uploadAndSaveAllegato(file, user));
        }
        existing.setPdfAllegato(existing.getAllegati().isEmpty() ? null : existing.getAllegati().get(0));

        Commessa updated = updateCommessa(id, existing);
        wsService.broadcast(Constants.MSG_REFRESH, null);
        return updated;
    }

    private List<MultipartFile> normalizeFiles(List<MultipartFile> files) {
        if (files == null) {
            return List.of();
        }
        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
    }

    private void removeAllegati(Commessa commessa, List<Long> allegatoIds) {
        List<Allegato> toRemove = commessa.getAllegati().stream()
                .filter(a -> allegatoIds.contains(a.getId()))
                .toList();
        for (Allegato allegato : toRemove) {
            allegato.setIsDeleted(true);
            allegatoRepository.save(allegato);
        }
        commessa.getAllegati().removeAll(toRemove);
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

        // Se supera 1 MB, tentiamo la compressione iterativa.
        if (originalBytes.length > MAX_BYTES) {
            byte[] compressed = compressPdfToLimit(originalBytes, MAX_BYTES);
            if (compressed == null) {
                throw new IllegalArgumentException("Il file PDF è troppo grande anche dopo la compressione (max 1 MB)");
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
        float[] qualities = new float[] { 0.75f, 0.6f, 0.5f, 0.4f, 0.3f, 0.25f };
        double[] scales = new double[] { 1.0, 0.9, 0.8, 0.7, 0.55, 0.4 };

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
                .filter(c -> c.getPdfAllegato() != null || (c.getAllegati() != null && !c.getAllegati().isEmpty()))
                .map(c -> {
                    Allegato allegato = c.getPdfAllegato() != null ? c.getPdfAllegato() : c.getAllegati().get(0);
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

    @Override
    public Optional<ResponseEntity<byte[]>> getAllegatoFile(Long commessaId, Long allegatoId) throws Exception {
        return getCommessaById(commessaId)
                .flatMap(c -> c.getAllegati().stream()
                        .filter(a -> a.getId().equals(allegatoId))
                        .findFirst())
                .map(allegato -> {
                    try {
                        byte[] fileBytes = s3Service.downloadFile(allegato.getStoragePath());
                        return ResponseEntity.ok()
                                .header("Content-Disposition", "inline; filename=\"" + allegato.getNomeFile() + "\"")
                                .contentType(MediaType.parseMediaType(allegato.getTipoFile()))
                                .body(fileBytes);
                    } catch (Exception e) {
                        throw new RuntimeException("Errore download allegato", e);
                    }
                });
    }
}
