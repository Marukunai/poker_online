package com.pokeronline.amigos.model;

import com.pokeronline.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "amistades",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_amistad_par", columnNames = {"usuario1_id", "usuario2_id"})
        },
        indexes = {
                @Index(name = "idx_amistades_usuario1", columnList = "usuario1_id"),
                @Index(name = "idx_amistades_usuario2", columnList = "usuario2_id")
        }
)
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Amistad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Invariante: usuario1_id < usuario2_id (se normaliza en @PrePersist / @PreUpdate)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario1_id", nullable = false)
    private User usuario1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario2_id", nullable = false)
    private User usuario2;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaAmistad = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean esFavorito1 = false;  // usuario1 marcó como favorito

    @Column(nullable = false)
    @Builder.Default
    private Boolean esFavorito2 = false;  // usuario2 marcó como favorito

    @Column(nullable = false)
    @Builder.Default
    private Boolean notificacionesActivas1 = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notificacionesActivas2 = true;

    private String alias1;  // Alias que usuario1 asigna a usuario2
    private String alias2;  // Alias que usuario2 asigna a usuario1

    @PrePersist
    @PreUpdate
    private void normalizarPar() {
        if (usuario1 != null && usuario2 != null && usuario1.getId() != null && usuario2.getId() != null) {
            if (usuario1.getId() > usuario2.getId()) {
                // Intercambiar para mantener usuario1_id < usuario2_id
                User tmp = usuario1;
                usuario1 = usuario2;
                usuario2 = tmp;

                // También intercambiar flags/alias por coherencia
                Boolean fav1 = esFavorito1; esFavorito1 = esFavorito2; esFavorito2 = fav1;
                Boolean not1 = notificacionesActivas1; notificacionesActivas1 = notificacionesActivas2; notificacionesActivas2 = not1;
                String a1 = alias1; alias1 = alias2; alias2 = a1;
            }
        }
        if (fechaAmistad == null) fechaAmistad = LocalDateTime.now();
    }
}