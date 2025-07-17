package com.pokeronline.estadisticas.dto;

import lombok.*;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EstadisticasUsuarioDTO {
    private Long userId;
    private int torneosJugados;
    private int torneosGanados;
    private double ratioVictoriasTorneo;
    private int vecesEliminadoPrimeraRonda;
    private int puntosTotales;
    private int fichasGanadas;
    private int mejorPosicion;
    private double posicionPromedio;
    private Date fechaUltimoTorneo;

    private int partidasSimplesJugadas;
    private int partidasSimplesGanadas;
    private int fichasGanadasSimples;

    private List<TorneoHistorialDTO> historialTorneos;
    private List<ProgresoMensualDTO> progresoMensual;
    private List<ProgresoMensualPartidasDTO> progresoMensualPartidas;

    private int torneosJugadosEquipo;
    private int torneosJugadosIndividual;
}