package com.pokeronline.torneo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EstadisticasTorneoDTO {
    private String username;
    private int torneosJugados;
    private int torneosGanados;
    private double ratioVictorias;
    private int vecesPrimeraRonda;
    private int puntosTotales;
    private int fichasGanadas;
    private int mejorPosicion;
    private double posicionPromedio;
    private String fechaUltimoTorneo;
}
