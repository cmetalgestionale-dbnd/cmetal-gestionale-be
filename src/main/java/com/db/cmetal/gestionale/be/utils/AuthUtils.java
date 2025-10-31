package com.db.cmetal.gestionale.be.utils;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.db.cmetal.gestionale.be.dto.CurrentUserDTO;
import com.db.cmetal.gestionale.be.entity.Utente;
import com.db.cmetal.gestionale.be.repository.UtenteRepository;

@Component
public class AuthUtils {

    private final UtenteRepository utenteRepository;

    public AuthUtils(UtenteRepository utenteRepository) {
        this.utenteRepository = utenteRepository;
    }

    /**
     * Restituisce CurrentUserDTO o lancia errori se non autenticato/autorizzato.
     * allowedRoles: passare i nomi dei ruoli (es. Constants.ROLE_ADMIN)
     */
    public CurrentUserDTO getCurrentUserOrThrow(String... allowedRoles) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            Utente utente = utenteRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente non trovato"));

            String ruoloUtente = Constants.getRoleName(utente.getLivello());
            if (Arrays.stream(allowedRoles).noneMatch(r -> r.equals(ruoloUtente))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Utente non autorizzato");
            }

            return CurrentUserDTO.fromUtente(ruoloUtente, utente.getId(), utente.getUsername(), utente.getNome(), utente.getCognome());
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente non autenticato");
    }
}
