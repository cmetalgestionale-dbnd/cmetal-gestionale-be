package com.db.cmetal.gestionale.be.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.db.cmetal.gestionale.be.dto.AssegnazioneDto;
import com.db.cmetal.gestionale.be.entity.Allegato;
import com.db.cmetal.gestionale.be.entity.Assegnazione;
import com.db.cmetal.gestionale.be.entity.Cliente;
import com.db.cmetal.gestionale.be.entity.Commessa;
import com.db.cmetal.gestionale.be.entity.Utente;
import com.db.cmetal.gestionale.be.repository.AllegatoRepository;
import com.db.cmetal.gestionale.be.repository.AssegnazioneRepository;
import com.db.cmetal.gestionale.be.repository.ClienteRepository;
import com.db.cmetal.gestionale.be.repository.CommessaRepository;
import com.db.cmetal.gestionale.be.repository.UtenteRepository;
import com.db.cmetal.gestionale.be.service.AssegnazioneService;
import com.db.cmetal.gestionale.be.service.SupabaseS3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AssegnazioneServiceImpl implements AssegnazioneService {

    private final AssegnazioneRepository assegnazioneRepository;
    private final UtenteRepository utenteRepository;
    private final CommessaRepository commessaRepository;
    private final ClienteRepository clienteRepository;
    private final AllegatoRepository allegatoRepository;
    private final SupabaseS3Service s3Service;

    @Override
    public List<Assegnazione> getAll() {
        return assegnazioneRepository.findAllByIsDeletedFalse();
    }

    @Override
    public List<Assegnazione> getByUtenteAndData(Long utenteId, LocalDate data) {
        OffsetDateTime startOfDay = data.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime endOfDay = data.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        return assegnazioneRepository.findVisibleByUtenteIdAndAssegnazioneAtBetween(utenteId, startOfDay, endOfDay);
    }


    @Override
    public Assegnazione getById(Long id) {
        return assegnazioneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assegnazione non trovata"));
    }

    @Override
    public Assegnazione createFromDto(AssegnazioneDto dto, Long assegnatoDaId, OffsetDateTime assegnazioneAt) {
        Utente utente = utenteRepository.findById(dto.getUtenteId())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        Commessa commessa = commessaRepository.findById(dto.getCommessaId())
                .orElseThrow(() -> new RuntimeException("Commessa non trovata"));
        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente non trovato"));
        Utente assegnatoDa = utenteRepository.findById(assegnatoDaId)
                .orElseThrow(() -> new RuntimeException("Utente assegnatore non trovato"));

        Assegnazione a = new Assegnazione();
        a.setUtente(utente);
        a.setCommessa(commessa);
        a.setCliente(cliente);
        a.setAssegnatoDa(assegnatoDa);
        a.setNote(dto.getNote());
        a.setAssegnazioneAt(assegnazioneAt);
        a.setCreatedAt(OffsetDateTime.now());
        a.setUpdatedAt(OffsetDateTime.now());
        a.setIsDeleted(false);
        return assegnazioneRepository.save(a);
    }

    @Override
    public Assegnazione updateFromDto(Long id, AssegnazioneDto dto) {
        Assegnazione existing = getById(id);

        existing.setUtente(utenteRepository.findById(dto.getUtenteId())
                .orElseThrow(() -> new RuntimeException("Utente non trovato")));
        existing.setCommessa(commessaRepository.findById(dto.getCommessaId())
                .orElseThrow(() -> new RuntimeException("Commessa non trovata")));
        existing.setCliente(clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente non trovato")));
        existing.setNote(dto.getNote());
        existing.setUpdatedAt(OffsetDateTime.now());

        return assegnazioneRepository.save(existing);
    }

    @Override
    public void softDelete(Long id) {
        Assegnazione a = getById(id);
        a.setIsDeleted(true);
        a.setUpdatedAt(OffsetDateTime.now());
        assegnazioneRepository.save(a);
    }
    
    @Override
    public Assegnazione startAssegnazione(Long id, Long utenteId) {
        Assegnazione a = assegnazioneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assegnazione non trovata"));
        // solo il dipendente assegnato può avviare
        if (!a.getUtente().getId().equals(utenteId)) {
            throw new RuntimeException("Non autorizzato");
        }
        a.setStartAt(OffsetDateTime.now());
        return assegnazioneRepository.save(a);
    }

    @Override
    public Assegnazione endAssegnazione(Long id, Long utenteId) {
        Assegnazione a = assegnazioneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assegnazione non trovata"));
        if (!a.getUtente().getId().equals(utenteId)) {
            throw new RuntimeException("Non autorizzato");
        }
        a.setEndAt(OffsetDateTime.now());
        return assegnazioneRepository.save(a);
    }
    
    @Override
    public Allegato uploadFoto(Long assegnazioneId, MultipartFile file, Long utenteId) throws Exception {
        Assegnazione a = getById(assegnazioneId);
        if (!a.getUtente().getId().equals(utenteId)) {
            throw new RuntimeException("Non autorizzato a caricare foto per questa assegnazione");
        }

        // VALIDAZIONE: file presente
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File mancante");
        }

        // VALIDAZIONE: tipo consentito
        String contentType = file.getContentType();
        if (contentType == null ||
            !(contentType.equalsIgnoreCase("image/jpeg")
              || contentType.equalsIgnoreCase("image/jpg")
              || contentType.equalsIgnoreCase("image/png")
              || contentType.equalsIgnoreCase("image/webp"))) {
            throw new IllegalArgumentException("Il file deve essere un'immagine (JPEG, PNG o WebP)");
        }

        // VALIDAZIONE: dimensione max 1 MB
        final long MAX_BYTES = 1_048_576L; // 1 MB
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("La foto non può superare 1 MB");
        }

        // se già presente un allegato, lo disabilitiamo
        if (a.getFotoAllegato() != null) {
            Allegato old = a.getFotoAllegato();
            old.setIsDeleted(true);
            allegatoRepository.save(old);
        }

        String path = "assegnazioni/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
        s3Service.uploadFile(file, path);

        Utente utente = utenteRepository.findById(utenteId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        Allegato allegato = new Allegato();
        allegato.setNomeFile(file.getOriginalFilename());
        allegato.setTipoFile(file.getContentType());
        allegato.setStoragePath(path);
        allegato.setCreatedAt(LocalDateTime.now());
        allegato.setCreatedBy(utente);
        allegato.setIsDeleted(false);

        Allegato saved = allegatoRepository.save(allegato);
        a.setFotoAllegato(saved);
        assegnazioneRepository.save(a);

        return saved;
    }


    @Override
    public Optional<ResponseEntity<byte[]>> getFotoFile(Long assegnazioneId) throws Exception {
        return Optional.ofNullable(getById(assegnazioneId).getFotoAllegato())
                .map(allegato -> {
                    try {
                        byte[] bytes = s3Service.downloadFile(allegato.getStoragePath());
                        return ResponseEntity.ok()
                                .header("Content-Disposition", "inline; filename=\"" + allegato.getNomeFile() + "\"")
                                .contentType(MediaType.parseMediaType(allegato.getTipoFile()))
                                .body(bytes);
                    } catch (Exception e) {
                        throw new RuntimeException("Errore download foto", e);
                    }
                });
    }
}
