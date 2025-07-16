package com.pokeronline.admin.dto;

import lombok.Data;

@Data
public class UpdateBlindLevelDTO {
    private int nivel;
    private int smallBlind;
    private int bigBlind;
    private int duracionSegundos;
}
