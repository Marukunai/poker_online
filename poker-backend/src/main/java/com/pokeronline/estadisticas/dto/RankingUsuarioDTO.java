package com.pokeronline.estadisticas.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RankingUsuarioDTO {
    private Long userId;
    private String username;
    private int puntosTotales;
    private int torneosGanados;
    private int torneosJugados;
    private int fichasGanadas;
}
