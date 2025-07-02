package com.pokeronline.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialMano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User jugador;

    @ManyToOne
    private Mesa mesa;

    private Date fecha;

    private int fichasGanadas;

    private boolean empate;

    private String tipoManoGanadora;

    private String cartasJugador;
    private String cartasComunitarias;

    private String cartasGanadoras;

    private String contraJugadores;

    @Enumerated(EnumType.STRING)
    private Fase faseFinal;
}