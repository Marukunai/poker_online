package com.pokeronline.torneo.equipos.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RankingEquipoDTO {
    private int posicion;
    private String nombreEquipo;
    private String nombreCapitan;
    private int puntosTotales;
}
