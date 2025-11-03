package com.db.cmetal.gestionale.be.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CommessaDto {
    public String codice;
    public String descrizione;
    public LocalDate dataCreazione;
}
