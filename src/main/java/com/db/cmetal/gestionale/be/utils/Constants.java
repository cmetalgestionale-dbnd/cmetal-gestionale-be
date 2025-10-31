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
    
    // Tipi messaggi WebSocket client
    public static final String MSG_ADD_ITEM_TEMP = "ADD_ITEM_TEMP";
    public static final String MSG_REMOVE_ITEM_TEMP = "REMOVE_ITEM_TEMP";
    public static final String MSG_UPDATE_TEMP = "UPDATE_TEMP";
    public static final String MSG_WARNING = "WARNING";
    public static final String MSG_ERROR = "ERROR";
    public static final String MSG_GET_STATUS = "GET_STATUS";
    public static final String MSG_UPDATE_TEMP_DELTA = "UPDATE_TEMP_DELTA";
    public static final String MSG_CLEAR_TEMP = "CLEAR_TEMP";

    public static final String ERR_LIMIT_PORTATE = "Limite portate raggiunto";
    public static final String ERR_LIMIT_PRODOTTO = "Limite per questo prodotto raggiunto (1 a persona per sessione)";
    public static final String ORDINE_STATO_INVIATO = "INVIATO";

    
    // Tipi messaggi WebSocket cucina
    public static final String MSG_CONSEGNA_CHANGED = "CONSEGNA_CHANGED";
    
    // Tipi messaggi WebSocket condivisi
    public static final String MSG_ORDER_SENT = "ORDER_SENT";
    public static final String MSG_REFRESH = "REFRESH";

    // OpenAPI / Swagger
    public static final String OPENAPI_SECURITY_SCHEME = "bearerAuth";
    public static final String OPENAPI_TITLE = "AYCE Blackout API";
    public static final String OPENAPI_VERSION = "1.0";
    public static final String OPENAPI_DESCRIPTION = "Documentazione delle API del sistema Blackout";

    // Sessioni / codici di stato
    public static final String SESSION_STATE_ACTIVE = "ATTIVA";

    // Error / response keys
    public static final String ERROR_KEY = "error";
}
