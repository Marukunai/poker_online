package com.pokeronline.torneo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private Date fechaInicio;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaFin;

    private int buyIn;
    private int fichasIniciales;
    private int premioTotal;

    @Column(name = "min_participantes")
    private Integer minParticipantes;
    private int maxParticipantes;
    private boolean eliminacionDirecta;

    @Enumerated(EnumType.STRING)
    private TorneoEstado estado;

    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL)
    private List<TorneoMesa> mesas;

    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL)
    private List<ParticipanteTorneo> participantes;

    @OneToMany(mappedBy = "torneo", cascade = CascadeType.ALL)
    private List<BlindLevel> blindLevels;

    private int nivelCiegasActual;
    private Date timestampInicioNivel;
}