package com.pokeronline.torneo.dto;

import com.pokeronline.model.User;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipanteTorneoDTO {
    private Long id;
    private Long torneoId;
    private String username;
    private int puntos;
    private int fichasActuales;
    private boolean eliminado;
}
