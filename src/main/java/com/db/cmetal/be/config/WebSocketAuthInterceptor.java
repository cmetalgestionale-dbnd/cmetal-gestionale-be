package com.db.cmetal.be.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.db.cmetal.be.security.jwt.JwtService;
import com.db.cmetal.be.utils.Constants;

import io.jsonwebtoken.Claims;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private static final Logger log = LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    private final JwtService jwtService;

    public WebSocketAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() != null && accessor.getCommand() == StompCommand.CONNECT) {
            // Legge il token dal header Authorization se presente
            String token = accessor.getFirstNativeHeader(Constants.HEADER_AUTHORIZATION);

            // Gestione caso "Bearer <token>"
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Se non c'è, prova dai cookie (usando "cookie" header)
            if (token == null) {
                String cookieHeader = accessor.getFirstNativeHeader(Constants.HEADER_COOKIE);
                if (cookieHeader != null) {
                    for (String c : cookieHeader.split(";")) {
                        String[] kv = c.trim().split("=", 2);
                        if (kv.length == 2 && kv[0].equals(Constants.COOKIE_TOKEN)) {
                            token = kv[1];
                            break;
                        }
                    }
                }
            }

            if (token != null) {
                try {
                    Claims claims = jwtService.extractAllClaims(token);
                    UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(claims, null, List.of());
                    accessor.setUser(auth);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception e) {
                    log.debug("WebSocket token non valido o parsing fallito: {}", e.getMessage());
                }
            }
        }
        return message;
    }
}
