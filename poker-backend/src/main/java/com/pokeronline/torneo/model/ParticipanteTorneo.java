package com.pokeronline.torneo.model;

import com.pokeronline.model.Mesa;
import com.pokeronline.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipanteTorneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Torneo torneo;

    @ManyToOne
    private User user;

    private int posicion;
    private boolean eliminado;
    private int fichasActuales;
    private int puntos;

    @ManyToOne
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;
}