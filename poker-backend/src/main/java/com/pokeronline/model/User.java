package com.pokeronline.model;

import com.pokeronline.bot.DificultadBot;
import com.pokeronline.bot.EstiloBot;
import com.pokeronline.moderacion.model.Sancion;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    private int rachaVictorias;
    private int rachaDerrotas;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Sancion> sanciones = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean bloqueado = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean chatBloqueado = false;

    // auxiliar para filtro
    public boolean estaBloqueado() {
        return bloqueado;
    }

    // auxiliar para filtro
    public boolean tieneChatBloqueado() {
        return chatBloqueado;
    }
}