package com.pokeronline.amigos.model;

import com.pokeronline.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "transferencias_fichas",
        indexes = {
                @Index(name = "idx_transf_remitente_fecha", columnList = "remitente_id,fecha"),
                @Index(name = "idx_transf_destinatario_fecha", columnList = "destinatario_id,fecha"),
                @Index(name = "idx_transf_estado", columnList = "estado")
        }
)
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaFichas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "remitente_id", nullable = false)
    private User remitente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private User destinatario;

    @Column(nullable = false)
    private Long cantidad;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fecha = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoTransferencia estado = EstadoTransferencia.PENDIENTE;

    private String mensaje;  // Mensaje opcional

    @Column(nullable = false)
    @Builder.Default
    private Boolean esRegalo = false;  // Si es regalo o préstamo

    // Opcional: comisión aplicada
    private Long feeAplicada;

    // Opcional: referencia para revertir (moderación)
    private Long transferenciaOriginalId;

    @PrePersist
    private void prePersist() {
        if (fecha == null) fecha = LocalDateTime.now();
        if (estado == null) estado = EstadoTransferencia.PENDIENTE;
    }
}