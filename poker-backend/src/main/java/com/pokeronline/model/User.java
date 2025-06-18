package com.pokeronline.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private String avatarUrl; // Ruta de la imagen del perfil

    private int fichas; // Fichas disponibles para apostar

    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder.Default
    private int partidasGanadas = 0;
}