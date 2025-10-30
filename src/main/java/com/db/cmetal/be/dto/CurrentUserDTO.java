package com.db.cmetal.be.dto;

/**
 * DTO unificato per rappresentare l'utente corrente o un client tavolo.
 * 
 * Se role = Constants.ROLE_CLIENT → sessioneId, tavoloId, tavoloNum, isAyce valorizzati.
 * Se role = ROLE_ADMIN o ROLE_DIPEN → userId e username valorizzati.
 */
public record CurrentUserDTO(
        String role,
        Long userId,
        String username,
        Long sessioneId,
        Integer tavoloId,
        Integer tavoloNum,
        Boolean isAyce
) {
    // Factory helper per utenti DB
    public static CurrentUserDTO fromUtente(String role, Long userId, String username) {
        return new CurrentUserDTO(role, userId, username, null, null, null, null);
    }

    // Factory helper per client tavolo
    public static CurrentUserDTO fromSessione(String role, Long sessioneId, Integer tavoloId, Integer tavoloNum, Boolean isAyce) {
        return new CurrentUserDTO(role, null, null, sessioneId, tavoloId, tavoloNum, isAyce);
    }
}
