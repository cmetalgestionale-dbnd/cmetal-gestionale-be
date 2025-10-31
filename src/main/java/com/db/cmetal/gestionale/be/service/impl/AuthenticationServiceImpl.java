package com.db.cmetal.gestionale.be.service.impl;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.db.cmetal.gestionale.be.dto.CurrentUserDTO;
import com.db.cmetal.gestionale.be.dto.LoginRequest;
import com.db.cmetal.gestionale.be.entity.Utente;
import com.db.cmetal.gestionale.be.repository.UtenteRepository;
import com.db.cmetal.gestionale.be.security.jwt.JwtService;
import com.db.cmetal.gestionale.be.service.AuthenticationService;
import com.db.cmetal.gestionale.be.utils.Constants;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UtenteRepository utenteRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationServiceImpl(AuthenticationManager authenticationManager,
                                     UtenteRepository utenteRepository,
                                     JwtService jwtService,
                                     PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.utenteRepository = utenteRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ResponseEntity<?> login(LoginRequest request, HttpServletResponse response) {
        try {
            // Authenticate with AuthenticationManager to check credentials.
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Load utente entity
            Optional<Utente> maybe = utenteRepository.findByUsername(request.getUsername());
            if (maybe.isEmpty()) {
                throw new BadCredentialsException("Utente non trovato");
            }
            Utente utente = maybe.get();

            if (!utente.getAttivo() || Boolean.TRUE.equals(utente.getIsDeleted())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Utente disabilitato");
            }

            // Generate token
            String token = jwtService.generateToken(utente);

            // Create cookie with same expiration as token
            long maxAgeSeconds = jwtService.getJwtExpirationMillis() / 1000L;
            Cookie cookie = new Cookie(Constants.COOKIE_TOKEN, token);
            cookie.setHttpOnly(true);
            cookie.setMaxAge((int) maxAgeSeconds);
            cookie.setPath("/");
            cookie.setSecure(false); // set true in prod if TLS/HTTPS
            // SameSite - not directly settable pre Servlet 4; if you need Strict/Lax set via header
            response.addCookie(cookie);

            // Return minimal user info
            CurrentUserDTO dto = CurrentUserDTO.fromUtente(Constants.getRoleName(utente.getLivello()), utente.getId(), utente.getUsername(), utente.getNome(), utente.getCognome());
            return ResponseEntity.ok(dto);

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenziali non valide");
        }
    }

    @Override
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Clear cookie
        Cookie cookie = new Cookie(Constants.COOKIE_TOKEN, "");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> getCurrentUser() {
        // Use SecurityContextHolder directly to build response or return 401
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            var username = ud.getUsername();
            var maybe = utenteRepository.findByUsername(username);
            if (maybe.isPresent()) {
                Utente u = maybe.get();
                CurrentUserDTO dto = CurrentUserDTO.fromUtente(Constants.getRoleName(u.getLivello()), u.getId(), u.getUsername(), u.getNome(), u.getCognome());
                return ResponseEntity.ok(dto);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utente non autenticato");
    }
}
