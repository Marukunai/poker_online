package com.pokeronline.model;

import com.pokeronline.bot.DificultadBot;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String avatarUrl; // Ruta de la imagen del perfil

    private int fichas; // Fichas totales del usuario fuera de cualquier mesa. Se actualiza cuando gana o pierde fichas en una partida.

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    private int partidasGanadas = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean esIA = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DificultadBot nivelBot = DificultadBot.FACIL;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstiloBot estiloBot = EstiloBot.DEFAULT;  // Nuevo campo en la entidad User

    @Builder.Default
    private int manosJugadas = 0;

    @Builder.Default
    private int manosGanadas = 0;

    @Builder.Default
    private int vecesAllIn = 0;

    @Builder.Default
    private int fichasGanadasHistoricas = 0;

    @Builder.Default
    private int vecesHizoBluff = 0;
}