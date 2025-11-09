package com.db.cmetal.gestionale.be.service.impl;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.db.cmetal.gestionale.be.service.RetentionService;
import com.db.cmetal.gestionale.be.service.SupabaseS3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RetentionServiceImpl implements RetentionService {

    private final AssegnazioneRepository assegnazioneRepository;
    private final CommessaRepository commessaRepository;
    private final ClienteRepository clienteRepository;
    private final UtenteRepository utenteRepository;
    private final AllegatoRepository allegatoRepository;
    private final SupabaseS3Service s3Service;
    private final JdbcTemplate jdbcTemplate;

    // Cancellazione hard assegnazioni (giornaliere)
    @Override
    public int cleanupAssegnazioni() {
        OffsetDateTime cutoff = OffsetDateTime.now().toLocalDate().atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        List<Assegnazione> toDelete = assegnazioneRepository
                .findByIsDeletedTrueOrAssegnazioneAtBefore(cutoff);

        int count = 0;
        for (Assegnazione a : toDelete) {
            Allegato allegato = a.getFotoAllegato();
            if (allegato != null) {
                try {
                    s3Service.deleteFile(allegato.getStoragePath());
                } catch (Exception e) {
                    System.err.println("Errore cancellazione file storage: " + e.getMessage());
                }
                allegatoRepository.delete(allegato);
            }
            assegnazioneRepository.delete(a);
            count++;
        }
        return count;
    }

    // Cancellazione hard commesse (manuale)
    @Override
    public int cleanupCommesse() {
        List<Commessa> commesse = commessaRepository.findByIsDeletedTrue();
        int count = 0;
        for (Commessa c : commesse) {
            // Elimina assegnazioni legate
            List<Assegnazione> assegnazioni = assegnazioneRepository.findByCommessaId(c.getId());
            for (Assegnazione a : assegnazioni) {
                a.setIsDeleted(true);
                assegnazioneRepository.save(a);
            }
            cleanupAssegnazioni();

            // Elimina pdf allegato (se presente)
            Allegato allegato = c.getPdfAllegato();
            if (allegato != null) {
                try {
                    s3Service.deleteFile(allegato.getStoragePath());
                } catch (Exception e) {
                    System.err.println("Errore cancellazione file PDF: " + e.getMessage());
                }
                allegatoRepository.delete(allegato);
            }

            commessaRepository.delete(c);
            count++;
        }
        return count;
    }

    // Cancellazione hard clienti
    @Override
    public int cleanupClienti() {
        List<Cliente> clienti = clienteRepository.findByIsDeletedTrue();
        int count = 0;
        for (Cliente cli : clienti) {
            List<Assegnazione> assegnazioni = assegnazioneRepository.findByClienteId(cli.getId());
            for (Assegnazione a : assegnazioni) {
                a.setIsDeleted(true);
                assegnazioneRepository.save(a);
            }
            cleanupAssegnazioni();
            clienteRepository.delete(cli);
            count++;
        }
        return count;
    }

    // Cancellazione hard utenti
    @Override
    public int cleanupUtenti() {
        List<Utente> utenti = utenteRepository.findByIsDeletedTrue();
        int count = 0;
        for (Utente u : utenti) {
            List<Assegnazione> assegnazioni = assegnazioneRepository.findByUtenteId(u.getId());
            for (Assegnazione a : assegnazioni) {
                a.setIsDeleted(true);
                assegnazioneRepository.save(a);
            }
            cleanupAssegnazioni();
            utenteRepository.delete(u);
            count++;
        }
        return count;
    }
    
    @Override
    public com.db.cmetal.gestionale.be.controller.RetentionController.SpaceUsageResponse getCurrentSpaceUsage() {
        // Ottieni dimensione DB
        String dbSize = jdbcTemplate.queryForObject(
            "SELECT pg_size_pretty(pg_database_size(current_database()))",
            String.class
        );

        // Ottieni dimensione totale storage (in formato human readable)
        String storageSize = s3Service.getTotalStorageUsagePretty();

        return new com.db.cmetal.gestionale.be.controller.RetentionController.SpaceUsageResponse(dbSize, storageSize);
    }
}
