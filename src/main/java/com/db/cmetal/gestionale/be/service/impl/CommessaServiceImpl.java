package com.db.cmetal.gestionale.be.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.db.cmetal.gestionale.be.entity.Allegato;
import com.db.cmetal.gestionale.be.entity.Commessa;
import com.db.cmetal.gestionale.be.repository.AllegatoRepository;
import com.db.cmetal.gestionale.be.repository.CommessaRepository;
import com.db.cmetal.gestionale.be.service.CommessaService;
import com.db.cmetal.gestionale.be.service.SupabaseS3Service;

@Service
public class CommessaServiceImpl implements CommessaService {

    private final CommessaRepository commessaRepository;
    private final AllegatoRepository allegatoRepository;
    private final SupabaseS3Service s3Service;

    public CommessaServiceImpl(CommessaRepository commessaRepository, SupabaseS3Service s3Service, AllegatoRepository allegatoRepository) {
        this.commessaRepository = commessaRepository;
		this.allegatoRepository = allegatoRepository;
		this.s3Service = s3Service;
    }

    @Override
    public Commessa saveCommessa(Commessa commessa) {
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
                    existing.setDataCreazione(commessa.getDataCreazione());
                    return commessaRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Commessa non trovata con id: " + id));
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
            c.setPdfAllegato(null);
        }
        commessaRepository.save(c);
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

    
    
}
