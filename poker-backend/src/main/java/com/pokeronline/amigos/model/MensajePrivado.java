package com.pokeronline.amigos.model;

import com.pokeronline.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "mensajes_privados",
        indexes = {
                @Index(name = "idx_conversacion", columnList = "remitente_id,destinatario_id,fecha_envio"),
                @Index(name = "idx_dest_leido", columnList = "destinatario_id,leido"),
                @Index(name = "idx_mp_fecha", columnList = "fecha_envio")
        }
)
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajePrivado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "remitente_id", nullable = false)
    private User remitente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private User destinatario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TipoMensaje tipo = TipoMensaje.TEXTO;

    @Column(columnDefinition = "TEXT")
    private String contenido;  // Texto, URL de audio/imagen/GIF

    @Column(name = "fecha_envio", nullable = false)
    @Builder.Default
    private LocalDateTime fechaEnvio = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private Boolean leido = false;

    private LocalDateTime fechaLectura;

    @Column(nullable = false)
    @Builder.Default
    private Boolean eliminadoPorRemitente = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean eliminadoPorDestinatario = false;

    // Para mensajes de audio
    private Integer duracionAudio;  // en segundos

    // Para respuestas a mensajes
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mensaje_respondido_id")
    private MensajePrivado mensajeRespondido;

    @PrePersist
    private void prePersist() {
        if (fechaEnvio == null) fechaEnvio = LocalDateTime.now();
    }
}