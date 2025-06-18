package com.pokeronline.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccionPartida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Mesa mesa;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    private Accion accion;

    private int cantidad;

    private Date timestamp;
}