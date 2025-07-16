package com.pokeronline.estadisticas.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProgresoMensualPartidasDTO {
    private Long userId;
    private String mes; // "2025-06"
    private int partidasJugadas;
    private int partidasGanadas;
    private int fichasGanadas;
}
