package com.db.cmetal.be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSalesDto {
    private Long prodottoId;
    private String nome;
    private Long quantita;
}