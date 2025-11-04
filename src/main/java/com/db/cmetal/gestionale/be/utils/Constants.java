package com.db.cmetal.gestionale.be.utils;

public final class Constants {
    private Constants() {}

    // Roles (string names)
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SUPERVISORE = "SUPERVISORE";
    public static final String ROLE_DIPENDENTE = "DIPENDENTE";

    // JWT claim keys
    public static final String CLAIM_SUB = "sub";
    public static final String CLAIM_ROLE = "role";

    // Cookie / header names
    public static final String COOKIE_TOKEN = "token";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_COOKIE = "cookie";

    // Swagger / static whitelist (esempio)
    public static final String[] SWAGGER_WHITELIST = new String[] {
    	"/auth/**",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/webjars/**",
        "/actuator/health"
    };

    // Helper: translate livello (int) -> role name
    public static String getRoleName(Integer livello) {
        if (livello == null) return "";
        return switch (livello) {
            case 0 -> ROLE_ADMIN;
            case 1 -> ROLE_SUPERVISORE;
            case 2 -> ROLE_DIPENDENTE;
            default -> "";
        };
    }
    
    // Tipi messaggi WebSocket
    public static final String MSG_REFRESH = "REFRESH";
    
    // Topics WebSocket
    public static final String BROADCAST = "/topic/broadcast";

    // OpenAPI / Swagger
    public static final String OPENAPI_SECURITY_SCHEME = "bearerAuth";
    public static final String OPENAPI_TITLE = "CMETAL API";
    public static final String OPENAPI_VERSION = "1.0";
    public static final String OPENAPI_DESCRIPTION = "Documentazione delle API del sistema CMETAL";
}
