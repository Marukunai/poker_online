package com.pokeronline.moderacion.model;

import com.pokeronline.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sancion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private TipoSancion tipo;

    @Enumerated(EnumType.STRING)
    private MotivoSancion motivo;

    private String descripcion; // Texto libre opcional

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInicio;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaFin; // Null si es permanente o advertencia

    private Long partidaId; // Puede usarse para sanciones en partidas simples o privadas
    private Long torneoId;

    private boolean activo;

    @ManyToOne
    private User adminQueSanciona; // o null si es automático
}