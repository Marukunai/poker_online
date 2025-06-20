package com.pokeronline.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JugadorMesaCompletoDTO {
    private Long id;
    private String username;
    private String avatarUrl;
    private int fichasEnMesa;
    private String carta1;
    private String carta2;
}
