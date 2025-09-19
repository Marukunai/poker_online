package com.pokeronline.torneo.dto;

import com.pokeronline.torneo.model.EsperaTorneo;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsperaTorneoDTO {
    private Long id;
    private Long torneoId;
    private String torneoNombre;
    private Long userId;
    private String username;
    private Date timestampIngreso;

    public static EsperaTorneoDTO fromEntity(EsperaTorneo e) {
        return EsperaTorneoDTO.builder()
                .id(e.getId())
                .torneoId(e.getTorneo().getId())
                .torneoNombre(e.getTorneo().getNombre())
                .userId(e.getUser().getId())
                .username(e.getUser().getUsername())
                .timestampIngreso(e.getTimestampIngreso())
                .build();
    }
}