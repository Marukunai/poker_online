package com.pokeronline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
@Builder
public class HistorialManoDTO {
    private Date fecha;
    private int fichasGanadas;
    private String cartasGanadoras;
    private String contraJugadores;
    private String tipoManoGanadora;
}