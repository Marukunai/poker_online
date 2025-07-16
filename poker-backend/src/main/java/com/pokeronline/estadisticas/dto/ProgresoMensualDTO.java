package com.pokeronline.estadisticas.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProgresoMensualDTO {
    private Long userId;
    private String mes; // Ej: "2025-06"
    private int torneosJugados;
    private int torneosGanados;
    private int partidasJugadas;
    private int partidasGanadas;
    private int puntosObtenidos;
}