package com.pokeronline.amigos.model;

import com.pokeronline.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "estado_presencia",
        indexes = {
                @Index(name = "idx_estado_presencia_estado", columnList = "estado"),
                @Index(name = "idx_estado_presencia_ultima_actividad", columnList = "ultima_actividad")
        }
)
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadoPresencia {

    // Primary Key compartida con User (1:1)
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoConexion estado = EstadoConexion.OFFLINE;

    private String detalleEstado;  // "Jugando en Mesa 12 / Torneo X"

    @Column(name = "ultima_actividad", nullable = false)
    @Builder.Default
    private LocalDateTime ultimaActividad = LocalDateTime.now();

    private Long mesaId;
    private Long torneoId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean aceptaInvitaciones = true;

    @PrePersist
    private void prePersist() {
        if (ultimaActividad == null) ultimaActividad = LocalDateTime.now();
        if (estado == null) estado = EstadoConexion.OFFLINE;
    }
}