package com.pokeronline.admin.dto;

import lombok.Data;

@Data
public class UpdateMesaDTO {
    private String nombre;
    private boolean activa;
    private int smallBlind;
    private int bigBlind;
    private int maxJugadores;
}
