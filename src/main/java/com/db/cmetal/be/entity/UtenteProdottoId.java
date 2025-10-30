package com.db.cmetal.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class UtenteProdottoId implements Serializable {

    private static final long serialVersionUID = 1L;

	@Column(name = "utente_id")
    private Long utenteId;

    @Column(name = "prodotto_id")
    private Long prodottoId;
}
