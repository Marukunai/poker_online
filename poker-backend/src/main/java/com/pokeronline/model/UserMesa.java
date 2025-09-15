package com.pokeronline.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserMesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JsonIgnore
    private Mesa mesa;

    private int fichasEnMesa;

    private boolean enJuego;

    private String carta1;
    private String carta2;

    private int totalApostado;

    @Enumerated(EnumType.STRING)
    private Posicion posicion;

    private boolean conectado;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastSeen;

    // Fichas que el usuario tenía al unirse a la mesa (sirve para restaurar o estadísticas)
    private int fichasIniciales;

    // Fichas que el usuario aún tiene disponibles para seguir apostando (sin incluir ya apostado)
    private int fichasDisponibles;

    private Integer fichasTemporalesAsignadas;
}