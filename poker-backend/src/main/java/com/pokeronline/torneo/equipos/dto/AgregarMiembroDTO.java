package com.pokeronline.torneo.equipos.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgregarMiembroDTO {
    private Long equipoId;
    private Long userId;
}
