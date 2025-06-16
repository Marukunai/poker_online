package com.pokeronline.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JugadorEnMesaDTO {
    private Long id;
    private String username;
    private String avatarUrl;
    private int fichasEnMesa;
}