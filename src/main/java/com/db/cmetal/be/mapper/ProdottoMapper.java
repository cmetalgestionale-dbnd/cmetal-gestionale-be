package com.db.cmetal.be.mapper;

import com.db.cmetal.be.dto.ProdottoDto;
import com.db.cmetal.be.entity.Prodotto;

public class ProdottoMapper {

    private ProdottoMapper() {
        // costruttore privato per evitare istanze
    }

    public static ProdottoDto toDto(Prodotto prodotto) {
        if (prodotto == null) return null;

        ProdottoDto dto = new ProdottoDto();
        dto.setId(prodotto.getId());
        dto.setNome(prodotto.getNome());
        dto.setDescrizione(prodotto.getDescrizione());
        dto.setPrezzo(prodotto.getPrezzo());
        dto.setCategoria(prodotto.getCategoria());
        dto.setIsPranzo(prodotto.getIsPranzo());
        dto.setIsCena(prodotto.getIsCena());
        dto.setIsAyce(prodotto.getIsAyce());
        dto.setIsCarta(prodotto.getIsCarta());
        dto.setIsLimitedPartecipanti(prodotto.getIsLimitedPartecipanti());

        return dto;
    }
}
