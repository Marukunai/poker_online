package com.pokeronline.torneo.equipos.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateCapitanDTO {
    private Long equipoId;
    private Long nuevoCapitanId;
}