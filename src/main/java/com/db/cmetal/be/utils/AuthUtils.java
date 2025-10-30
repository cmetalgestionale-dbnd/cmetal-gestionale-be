package com.db.cmetal.be.utils;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.db.cmetal.be.dto.CurrentUserDTO;
import com.db.cmetal.be.entity.Sessione;
import com.db.cmetal.be.entity.Utente;
import com.db.cmetal.be.repository.UtenteRepository;
import com.db.cmetal.be.service.SessioneService;

import io.jsonwebtoken.Claims;

@Component
public class AuthUtils {

    private final UtenteRepository utenteRepository;
    private final SessioneService sessioneService;

    public AuthUtils(UtenteRepository utenteRepository, SessioneService sessioneService) {
        this.utenteRepository = utenteRepository;
        this.sessioneService = sessioneService;
    }

    /**
     * Restituisce un CurrentUserDTO con i dati dell'utente o client tavolo autenticato.
     * Lancia ResponseStatusException se non autenticato o non autorizzato.
     * 
     * @param allowedRoles Lista ruoli ammessi (es: Constants.ROLE_ADMIN, ROLE_DIPEN, ROLE_CLIENT)
     */
    public CurrentUserDTO getCurrentUserOrThrow(String... allowedRoles) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof Claims claims) { // token CLIENT
            String role = claims.get(Constants.CLAIM_ROLE, String.class);
            if (Arrays.stream(allowedRoles).noneMatch(r -> r.equals(role))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accesso negato");
            }

            if (Constants.ROLE_CLIENT.equals(role)) {
                Long sessioneId = claims.get(Constants.CLAIM_SESSIONE_ID, Long.class);
                Sessione sessione = sessioneService.findById(sessioneId);
                if (sessione == null || !Constants.SESSION_STATE_ACTIVE.equals(sessione.getStato())) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessione non attiva");
                }

                return CurrentUserDTO.fromSessione(
                        Constants.ROLE_CLIENT,
                        sessione.getId(),
                        sessione.getTavolo().getId(),
                        sessione.getTavolo().getNumero(),
                        sessione.getIsAyce()
                );
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token non valido");
        } else if (principal instanceof UserDetails userDetails) { // utenti registrati
            Utente utente = utenteRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente non trovato"));

            String ruoloUtente = Constants.getRoleName(utente.getLivello());
            if (Arrays.stream(allowedRoles).noneMatch(r -> r.equals(ruoloUtente))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Utente non autorizzato");
            }

            return CurrentUserDTO.fromUtente(ruoloUtente, utente.getId(), utente.getUsername());
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utente non autenticato");
    }
}
