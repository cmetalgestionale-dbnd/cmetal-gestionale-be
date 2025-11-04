package com.db.cmetal.gestionale.be.service;

import com.db.cmetal.gestionale.be.dto.CommessaDto;
import com.db.cmetal.gestionale.be.entity.Commessa;
import com.db.cmetal.gestionale.be.entity.Utente;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface CommessaService {
    Commessa saveCommessa(Commessa commessa, Utente user);
    List<Commessa> getAllCommesse();
    Optional<Commessa> getCommessaById(Long id);
    Optional<Commessa> getCommessaByCodice(String codice);
    Commessa updateCommessa(Long id, Commessa commessa);
    void deleteCommessa(Long id);
	void hardDeleteCommessa(Long id);
	void restoreCommessa(Long id);
	Commessa createCommessa(CommessaDto dto, MultipartFile file, Utente user) throws Exception;
	Commessa updateCommessaWithFile(Long id, CommessaDto dto, MultipartFile file, Boolean removeFile, Utente user)
			throws Exception;
	Optional<String> getAllegatoUrl(Long id);
	Optional<ResponseEntity<byte[]>> getAllegatoFile(Long id) throws Exception;
}
