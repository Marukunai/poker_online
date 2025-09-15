package com.pokeronline.chat.model;

import com.pokeronline.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MensajeChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contenido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User remitente;

    private Long mesaId; // o torneoId, si es torneo

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
}