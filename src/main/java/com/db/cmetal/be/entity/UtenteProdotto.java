package com.db.cmetal.be.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "utente_prodotto")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UtenteProdotto {

    @EmbeddedId
    private UtenteProdottoId id;

    @Column(name = "riceve_comanda", nullable = false)
    private boolean riceveComanda = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("prodottoId") // collega la parte della chiave composita prodottoId
    @JoinColumn(name = "prodotto_id", insertable = false, updatable = false)
    @JsonIgnore
    private Prodotto prodotto;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("utenteId")
    @JoinColumn(name = "utente_id", insertable = false, updatable = false)
    @JsonIgnore
    private Utente utente;
    
    // COSTRUTTORE UTILE
    public UtenteProdotto(UtenteProdottoId id, boolean riceveComanda) {
        this.id = id;
        this.riceveComanda = riceveComanda;
    }
}
