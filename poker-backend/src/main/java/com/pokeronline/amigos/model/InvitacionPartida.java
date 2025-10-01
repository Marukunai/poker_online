package com.pokeronline.amigos.model;

import com.pokeronline.model.Mesa;
import com.pokeronline.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "invitaciones_partida",
        uniqueConstraints = {
                // Evita duplicar invitaciones PENDIENTE para mismo destinatario/mesa/tipo
                @UniqueConstraint(
                        name = "uk_inv_pendiente_dest_mesa_tipo",
                        columnNames = {"destinatario_id", "mesa_id", "tipo", "estado"}
                )
        },
        indexes = {
                @Index(name = "idx_inv_dest_estado", columnList = "destinatario_id,estado"),
                @Index(name = "idx_inv_fecha_envio", columnList = "fecha_envio")
        }
)
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitacionPartida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "remitente_id", nullable = false)
    private User remitente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private User destinatario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mesa_id", nullable = false)
    private Mesa mesa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TipoInvitacion tipo = TipoInvitacion.JUGADOR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoInvitacion estado = EstadoInvitacion.PENDIENTE;

    @Column(name = "fecha_envio", nullable = false)
    @Builder.Default
    private LocalDateTime fechaEnvio = LocalDateTime.now();

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    private String mensaje;

    @PrePersist
    private void prePersist() {
        if (fechaEnvio == null) fechaEnvio = LocalDateTime.now();
        // Si quieres expiración por defecto ~10 min:
        // if (fechaExpiracion == null) fechaExpiracion = fechaEnvio.plusMinutes(10);
    }
}