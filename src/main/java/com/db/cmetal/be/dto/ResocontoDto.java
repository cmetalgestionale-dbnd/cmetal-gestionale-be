package com.db.cmetal.be.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResocontoDto {
	private Long id;
    private String nome;             // Nome prodotto o "Quota AYCE" / "Totale finale"
    private int quantita;            // Quantità o numero partecipanti
    private double prezzoUnitario;   // Prezzo unitario
    private double totale;           // Totale riga
    private LocalDateTime orario;    // Orario ordine (null per AYCE e Totale finale)
    private Integer tavolo;          // Numero tavolo
    private String stato;            // Stato consegna (null per AYCE e Totale finale)
}
