package com.pokeronline.torneo.equipos.model;

import com.pokeronline.model.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiembroEquipoTorneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private EquipoTorneo equipo;

    @ManyToOne
    private User user;

    private boolean esCapitan;
}