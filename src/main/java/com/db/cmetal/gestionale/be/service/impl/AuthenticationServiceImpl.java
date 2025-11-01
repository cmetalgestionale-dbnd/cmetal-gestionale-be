package com.db.cmetal.gestionale.be.service.impl;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.db.cmetal.gestionale.be.dto.CurrentUserDTO;
import com.db.cmetal.gestionale.be.dto.LoginRequest;
import com.db.cmetal.gestionale.be.entity.Utente;
import com.db.cmetal.gestionale.be.repository.UtenteRepository;
import com.db.cmetal.gestionale.be.security.jwt.JwtService;
import com.db.cmetal.gestionale.be.service.AuthenticationService;
import com.db.cmetal.gestionale.be.utils.Constants;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UtenteRepository utenteRepository;
    private final JwtService jwtService;

    // 🔧 CAMBIA QUESTO VALORE PER PASSARE DA LOCALE A PRODUZIONE
    private static final boolean IS_PRODUCTION = true;

    public AuthenticationServiceImpl(AuthenticationManager authenticationManager,
                                     UtenteRepository utenteRepository,
                                     JwtService jwtService,
                                     PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.utenteRepository = utenteRepository;
        this.jwtService = jwtService;
    }

    @Override
    public ResponseEntity<?> login(LoginRequest request, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            Optional<Utente> maybe = utenteRepository.findByUsername(request.getUsername());
            if (maybe.isEmpty()) {
                throw new BadCredentialsException("Utente non trovato");
            }
            Utente utente = maybe.get();

            if (!utente.getAttivo() || Boolean.TRUE.equals(utente.getIsDeleted())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Utente disabilitato");
            }

            String token = jwtService.generateToken(utente);
            long maxAgeSeconds = jwtService.getJwtExpirationMillis() / 1000L;

            String domain = IS_PRODUCTION ? ".castellanometal.com" : null;
            boolean secure = IS_PRODUCTION;
            String sameSite = IS_PRODUCTION ? "None" : "Lax";

            StringBuilder cookieBuilder = new StringBuilder();
            cookieBuilder.append(Constants.COOKIE_TOKEN)
                    .append("=")
                    .append(token)
                    .append("; Max-Age=")
                    .append(maxAgeSeconds)
                    .append("; Path=/; HttpOnly; SameSite=")
                    .append(sameSite);

            if (secure) cookieBuilder.append("; Secure");
            if (domain != null) cookieBuilder.append("; Domain=").append(domain);

            response.setHeader("Set-Cookie", cookieBuilder.toString());

            // 🔹 Redireziona direttamente il browser
            response.setStatus(HttpServletResponse.SC_FOUND); // 302
            response.setHeader("Location", "https://app.castellanometal.com/");

            return ResponseEntity.status(HttpStatus.FOUND).build();

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenziali non valide");
        }
    }


    @Override
    public ResponseEntity<?> logout(HttpServletResponse response) {
        String domain = IS_PRODUCTION ? ".castellanometal.com" : null;
        boolean secure = IS_PRODUCTION;
        String sameSite = IS_PRODUCTION ? "None" : "Lax";

        StringBuilder cookieBuilder = new StringBuilder();
        cookieBuilder.append(Constants.COOKIE_TOKEN)
                .append("=; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Path=/; HttpOnly; SameSite=")
                .append(sameSite);

        if (secure) cookieBuilder.append("; Secure");
        if (domain != null) cookieBuilder.append("; Domain=").append(domain);

        response.setHeader("Set-Cookie", cookieBuilder.toString());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> getCurrentUser() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            var username = ud.getUsername();
            var maybe = utenteRepository.findByUsername(username);
            if (maybe.isPresent()) {
                Utente u = maybe.get();
                CurrentUserDTO dto = CurrentUserDTO.fromUtente(
                        Constants.getRoleName(u.getLivello()),
                        u.getId(),
                        u.getUsername(),
                        u.getNome(),
                        u.getCognome()
                );
                return ResponseEntity.ok(dto);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
    }
}
