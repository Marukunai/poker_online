package com.pokeronline.torneo.equipos.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrearEquipoDTO {
    private Long torneoId;
    private String nombreEquipo;
    private Long capitanId;
}