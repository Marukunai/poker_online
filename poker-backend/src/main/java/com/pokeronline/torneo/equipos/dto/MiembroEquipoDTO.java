package com.pokeronline.torneo.equipos.dto;

import com.pokeronline.torneo.equipos.model.MiembroEquipoTorneo;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MiembroEquipoDTO {
    private Long id;
    private Long equipoId;
    private Long userId;
    private String username;
    private boolean esCapitan;

    public static MiembroEquipoDTO fromEntity(MiembroEquipoTorneo miembro) {
        return MiembroEquipoDTO.builder()
                .id(miembro.getId())
                .equipoId(miembro.getEquipo().getId())
                .userId(miembro.getUser().getId())
                .username(miembro.getUser().getUsername())
                .esCapitan(miembro.isEsCapitan())
                .build();
    }
}