package com.db.cmetal.be.utils;

import java.util.Map;

public final class Constants {
    private Constants() {}

    // Mappa dei ruoli dal DB -> nome ruolo
    public static final Map<Integer, String> ROLE_MAP = Map.of(
        0, "ADMIN",
        1, "DIPEN"
    );
    
    // Costanti per ruoli legati al DB
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_DIPEN = "DIPEN";

    // Ruolo speciale per il client tavolo, non legato al DB
    public static final String ROLE_CLIENT = "CLIENT";

    /**
     * Restituisce il nome ruolo corrispondente al livello DB.
     * Se non esiste, lancia eccezione (fail fast)
     */
    public static String getRoleName(int livello) {
        String role = ROLE_MAP.get(livello);
        if (role == null) throw new IllegalArgumentException("Ruolo non valido per livello: " + livello);
        return role;
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
    
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_COOKIE = "cookie";
    public static final String COOKIE_TOKEN = "token";

    // OpenAPI / Swagger
    public static final String OPENAPI_SECURITY_SCHEME = "bearerAuth";
    public static final String OPENAPI_TITLE = "AYCE Blackout API";
    public static final String OPENAPI_VERSION = "1.0";
    public static final String OPENAPI_DESCRIPTION = "Documentazione delle API del sistema Blackout";

    // Security / Swagger white list (usata in SecurityConfig)
    public static final String[] SWAGGER_WHITELIST = new String[] {
        "/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**"
    };

    // Sessioni / codici di stato
    public static final String SESSION_STATE_ACTIVE = "ATTIVA";

    // Error / response keys
    public static final String ERROR_KEY = "error";

    // Claim keys per JWT
    public static final String CLAIM_SUB = "sub";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_TAVOLO_ID = "tavoloId";
    public static final String CLAIM_TAVOLO_NUM = "tavoloNum";
    public static final String CLAIM_SESSIONE_ID = "sessioneId";
    public static final String CLAIM_IS_AYCE = "isAyce";
}
