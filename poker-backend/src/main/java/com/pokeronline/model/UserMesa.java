package com.pokeronline.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMesa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Mesa mesa;

    private int fichasEnMesa;

    private boolean enJuego;

    private String carta1;
    private String carta2;

    @Enumerated(EnumType.STRING)
    private Posicion posicion;
}