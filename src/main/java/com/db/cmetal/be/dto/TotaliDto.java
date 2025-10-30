package com.db.cmetal.be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotaliDto {
    private double lordo;
    private double netto;
    private int sessioniAyce;
    private int sessioniCarta;
    private int aycePranzi;
    private int ayceCene;
    private int personeAycePranzo;
    private int personeAyceCena;
    private int personeCarta;
}
