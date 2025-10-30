package com.db.cmetal.be.security.jwt;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.db.cmetal.be.entity.Sessione;
import com.db.cmetal.be.service.CustomUserDetailsService;
import com.db.cmetal.be.service.SessioneService;
import com.db.cmetal.be.utils.Constants;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final SessioneService sessioneService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService, SessioneService sessioneService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.sessioneService = sessioneService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String jwt = null;
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (Constants.COOKIE_TOKEN.equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if (jwt == null) {
            String authHeader = request.getHeader(Constants.HEADER_AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
            }
        }

        if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = jwtService.extractAllClaims(jwt);
                String role = claims.get(Constants.CLAIM_ROLE, String.class);

                if (Constants.ROLE_CLIENT.equals(role)) {
                    Long sessioneId = claims.get(Constants.CLAIM_SESSIONE_ID, Long.class);
                    Sessione sessione = sessioneService.findById(sessioneId);
                    if (sessione != null && Constants.SESSION_STATE_ACTIVE.equals(sessione.getStato())) {
                        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(claims, null, authorities);
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } else {
                    String username = claims.getSubject();
                    if (username != null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        if (jwtService.isTokenValid(jwt, userDetails)) {
                            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                    }
                }
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                // Token scaduto → ignoralo, l'app prosegue come se non esistesse
                log.debug("JWT scaduto: {}", e.getMessage());
            } catch (Exception e) {
                log.debug("Errore parsing JWT: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
