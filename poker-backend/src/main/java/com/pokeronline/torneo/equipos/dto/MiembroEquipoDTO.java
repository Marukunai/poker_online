package com.pokeronline.torneo.equipos.dto;

import com.pokeronline.torneo.equipos.model.MiembroEquipoTorneo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MiembroEquipoDTO {
    private Long id;
    private Long equipoId;
    private Long userId;
    private String username;

    public static MiembroEquipoDTO fromEntity(MiembroEquipoTorneo miembro) {
        return MiembroEquipoDTO.builder()
                .id(miembro.getId())
                .equipoId(miembro.getEquipo().getId())
                .userId(miembro.getUser().getId())
                .username(miembro.getUser().getUsername())
                .build();
    }
}
