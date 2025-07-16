package com.pokeronline.torneo.equipos.dto;

import com.pokeronline.torneo.equipos.model.EquipoTorneo;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EquipoTorneoDTO {
    private Long id;
    private String nombre;
    private Long torneoId;
    private Long capitanId;
    private String nombreCapitan;
    private int puntosTotales;

    public static EquipoTorneoDTO fromEntity(EquipoTorneo equipo) {
        return EquipoTorneoDTO.builder()
                .id(equipo.getId())
                .torneoId(equipo.getTorneo().getId())
                .nombre(equipo.getNombre())
                .capitanId(equipo.getCapitan().getId())
                .nombreCapitan(equipo.getCapitan().getUsername())
                .puntosTotales(equipo.getPuntosTotales())
                .build();
    }
}