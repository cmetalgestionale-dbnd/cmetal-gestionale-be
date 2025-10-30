package com.db.cmetal.be.service.impl;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.db.cmetal.be.dto.LoginRequest;
import com.db.cmetal.be.entity.Sessione;
import com.db.cmetal.be.entity.Tavolo;
import com.db.cmetal.be.entity.Utente;
import com.db.cmetal.be.repository.UtenteRepository;
import com.db.cmetal.be.security.jwt.JwtService;
import com.db.cmetal.be.service.AuthenticationService;
import com.db.cmetal.be.service.SessioneService;
import com.db.cmetal.be.service.TavoloService;
import com.db.cmetal.be.utils.Constants;

import java.util.Map;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UtenteRepository utenteRepository;
    private final TavoloService tavoloService;
    private final SessioneService sessioneService;

    public AuthenticationServiceImpl(AuthenticationManager authManager, JwtService jwtService,
                                     UtenteRepository utenteRepository, TavoloService tavoloService,
                                     SessioneService sessioneService) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.utenteRepository = utenteRepository;
        this.tavoloService = tavoloService;
        this.sessioneService = sessioneService;
    }

    @Override
    public ResponseEntity<?> login(LoginRequest request, HttpServletResponse response) {
        var auth = new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
        authManager.authenticate(auth);

        Utente utente = utenteRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));
        String token = jwtService.generateToken(utente);

        Cookie cookie = new Cookie(Constants.COOKIE_TOKEN, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60);
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> loginTavolo(Integer tavoloNum, HttpServletResponse response) {
        Tavolo tavolo = tavoloService.findByNumero(tavoloNum);
        if (tavolo == null || !Boolean.TRUE.equals(tavolo.getAttivo())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND) //404
                    .body(Map.of(Constants.ERROR_KEY, "Tavolo non trovato o non attivo"));
        }

        Sessione sessioneAttiva = sessioneService.findAll().stream()
                .filter(s -> s.getTavolo().getId().equals(tavolo.getId()))
                .filter(s -> Constants.SESSION_STATE_ACTIVE.equals(s.getStato()))
                .findFirst()
                .orElse(null);

        if (sessioneAttiva == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT) // 409
                    .body(Map.of(Constants.ERROR_KEY, "Nessuna sessione attiva per questo tavolo"));
        }
        
        // --- NUOVA LOGICA: se la richiesta è fatta da uno user già autenticato (staff),
        // non generare/aggiungere il cookie JWT "client" (non sovrascrivere il cookie esistente) ---
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            // Utente "vero" (staff) — restituisci i dati della sessione, senza toccare i cookie
            return ResponseEntity.ok(Map.of(
                    "tavoloId", tavolo.getId(),
                    "numeroTavolo", tavolo.getNumero(),
                    "sessioneId", sessioneAttiva.getId(),
                    "isAyce", sessioneAttiva.getIsAyce()
            ));
        }
        

        String token = jwtService.generateClientToken(sessioneAttiva);
        Cookie cookie = new Cookie(Constants.COOKIE_TOKEN, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(2 * 60 * 60);
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of(
                "tavoloId", tavolo.getId(),
                "numeroTavolo", tavolo.getNumero(),
                "sessioneId", sessioneAttiva.getId(),
                "isAyce", sessioneAttiva.getIsAyce()
        ));
    }

    @Override
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie(Constants.COOKIE_TOKEN, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof Claims claims &&
                Constants.ROLE_CLIENT.equals(claims.get(Constants.CLAIM_ROLE, String.class))) {

            Long sessioneId = claims.get(Constants.CLAIM_SESSIONE_ID, Long.class);
            Sessione sessione = sessioneService.findById(sessioneId);
            if (sessione == null || !Constants.SESSION_STATE_ACTIVE.equals(sessione.getStato())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            return ResponseEntity.ok(Map.of(
                    "role", Constants.ROLE_CLIENT,
                    "tavoloId", sessione.getTavolo().getId(),
                    "tavoloNum", sessione.getTavolo().getNumero(),
                    "sessioneId", sessione.getId(),
                    "isAyce", sessione.getIsAyce()
            ));
        } else if (principal instanceof UserDetails userDetails) {
            Utente utente = utenteRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utente non trovato"));

            
            return ResponseEntity.ok(Map.of(
                    "role", Constants.getRoleName(utente.getLivello()),
                    "id", utente.getId(),
                    "username", utente.getUsername()
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
