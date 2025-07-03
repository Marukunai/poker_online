package com.pokeronline.model;

import com.pokeronline.bot.DificultadBot;
import com.pokeronline.bot.EstiloBot;
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

    private String avatarUrl;

    private int fichas;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    private int partidasGanadas = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean esIA = false;

    @Enumerated(EnumType.STRING)
    private DificultadBot nivelBot;

    @Enumerated(EnumType.STRING)
    private EstiloBot estiloBot;

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