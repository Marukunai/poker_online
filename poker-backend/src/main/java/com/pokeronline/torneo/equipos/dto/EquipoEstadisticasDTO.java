package com.pokeronline.torneo.equipos.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquipoEstadisticasDTO {
    private Long equipoId;
    private String nombreEquipo;
    private int torneosJugados;
    private int torneosGanados;
    private double posicionPromedio;
    private int mejorPosicion;
    private int puntosTotales;
    private Date fechaUltimoTorneo;
}