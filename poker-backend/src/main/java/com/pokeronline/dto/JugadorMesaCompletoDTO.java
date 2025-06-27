package com.pokeronline.dto;

import com.pokeronline.model.Posicion;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JugadorMesaCompletoDTO {
    private Long id;
    private String username;
    private String avatarUrl;
    private int fichasEnMesa;
    private int fichasTotales;
    private String carta1;
    private String carta2;
    private Posicion posicion;
}
