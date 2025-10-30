package com.db.cmetal.be.service;

import java.time.LocalDate;
import java.util.List;

import com.db.cmetal.be.dto.ProductSalesDto;
import com.db.cmetal.be.dto.SessionDeltaDto;
import com.db.cmetal.be.dto.TotaliDto;

public interface StatisticheService {
    TotaliDto calcolaTotali(String period, LocalDate from, LocalDate to);

    Integer contaSessioni(String period, LocalDate from, LocalDate to);

    List<ProductSalesDto> prodottiPiùVenduti(String period, LocalDate from, LocalDate to, int limit);

    List<ProductSalesDto> prodottiMenoVenduti(String period, LocalDate from, LocalDate to, int limit);

    SessionDeltaDto deltaSessione(Long sessioneId);

    // utilità: totali per singola sessione (usata internamente e può essere esposta)
    TotaliDto calcolaTotaliPerSessioneId(Long sessioneId);
}
