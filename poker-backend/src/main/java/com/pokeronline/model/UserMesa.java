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
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;

    private int fichasEnMesa;

    private boolean enJuego;

    private String carta1; // Representación simple como "AS", "10H", "QC"
    private String carta2;
}
