package com.pokeronline.amigos.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferirFichasDTO {
    private Long destinatarioId;
    private Long cantidad;
    private String mensaje;
    private Boolean esRegalo; // true: regalo, false: préstamo
}
