package com.pokeronline.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Mesa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private boolean activa;
    private int maxJugadores;

    private int smallBlind;
    private int bigBlind;

    // Cartas comunitarias
    private String flop1;
    private String flop2;
    private String flop3;
    private String turn;
    private String river;

    private int pot;

    @Enumerated(EnumType.STRING)
    private Fase fase;

    @OneToMany(mappedBy = "mesa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserMesa> jugadores;

    private Boolean privada;
    private String codigoAcceso;

    private boolean fichasTemporales;

}