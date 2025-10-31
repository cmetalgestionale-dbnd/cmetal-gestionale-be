package com.db.cmetal.gestionale.be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserDTO {
    private String role;
    private Long id;
    private String username;
    private String nome;
    private String cognome;

    public static CurrentUserDTO fromUtente(String role, Long id, String username, String nome, String cognome) {
        return new CurrentUserDTO(role, id, username, nome, cognome);
    }
}
