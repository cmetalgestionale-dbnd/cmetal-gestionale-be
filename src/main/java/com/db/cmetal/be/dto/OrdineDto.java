package com.db.cmetal.be.dto;

import java.time.LocalDateTime;

import com.db.cmetal.be.entity.Tavolo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdineDto {
    private Long id;
    private Tavolo tavolo;
    private ProdottoDto prodotto;
    private Integer quantita;
    private LocalDateTime orario;
    private Boolean flagConsegnato;
    private Integer numeroPartecipanti;
}
