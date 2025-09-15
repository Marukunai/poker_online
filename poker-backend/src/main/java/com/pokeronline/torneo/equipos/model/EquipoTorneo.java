package com.pokeronline.torneo.equipos.model;

import com.pokeronline.model.User;
import com.pokeronline.torneo.model.Torneo;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipoTorneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @ManyToOne
    private Torneo torneo;

    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL)
    private List<MiembroEquipoTorneo> miembros;

    private int puntosTotales;

    private boolean eliminado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User capitan;
}