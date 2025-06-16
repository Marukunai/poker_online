package com.pokeronline.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Carta {
    private String valor;   // A, 2, 3, ..., K
    private String palo;    // ♠️, ♥️, ♦️, ♣️ (S, H, D, C)

    @Override
    public String toString() {
        return valor + palo;
    }
}
