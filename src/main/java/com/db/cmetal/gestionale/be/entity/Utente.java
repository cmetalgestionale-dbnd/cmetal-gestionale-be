package com.db.cmetal.gestionale.be.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "utente")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utente implements UserDetails {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(nullable = false)
    private Integer livello; // 0=admin,1=supervisore,2=dipendente

    private String nome;
    private String cognome;

    @Column(name = "tariffa_oraria")
    private Double tariffaOraria;

    private String email;
    private String telefono;

    @Column(nullable = false)
    private Boolean attivo;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return switch (livello) {
            case 0 -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
            case 1 -> List.of(new SimpleGrantedAuthority("ROLE_SUPERVISORE"));
            case 2 -> List.of(new SimpleGrantedAuthority("ROLE_DIPENDENTE"));
            default -> List.of();
        };
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return attivo && !isDeleted; }
}
