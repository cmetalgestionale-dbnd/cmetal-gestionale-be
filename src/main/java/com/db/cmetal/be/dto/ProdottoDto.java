package com.db.cmetal.be.dto;

import com.db.cmetal.be.entity.Categoria;

import lombok.Data;

@Data
public class ProdottoDto {
	private Long id;
	private String nome;
	private String descrizione;
	private Double prezzo;
	private Categoria categoria;
	private Boolean isPranzo;
	private Boolean isCena;
	private Boolean isAyce;
	private Boolean isCarta;
	private Boolean isLimitedPartecipanti;
}
