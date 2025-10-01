package com.pokeronline.amigos.model;

import com.pokeronline.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "configuracion_privacidad",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_privacidad_user", columnNames = {"user_id"})
        }
)
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionPrivacidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NivelPrivacidad quienPuedeEnviarSolicitudes = NivelPrivacidad.TODOS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NivelPrivacidad quienPuedeVerEstado = NivelPrivacidad.AMIGOS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NivelPrivacidad quienPuedeInvitar = NivelPrivacidad.AMIGOS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NivelPrivacidad quienPuedeTransferirFichas = NivelPrivacidad.AMIGOS;

    @Column(nullable = false)
    @Builder.Default
    private Boolean mostrarEstadisticas = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean aceptarSolicitudesAutomaticamente = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notificarConexion = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notificarInicioPartida = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean modoPerturbacion = false;  // No molestar
}