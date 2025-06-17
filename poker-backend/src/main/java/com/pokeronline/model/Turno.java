package com.pokeronline.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Mesa mesa;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private Accion accion;

    private int apuesta;

    private int ordenTurno;

    private boolean activo;     // Si es su turno actual

    private boolean eliminado; // Si hizo fold o se quedó sin fichas
}