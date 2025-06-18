package com.pokeronline.dto;

import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@Builder
@Getter
@Setter
public class HistorialManoDTO {
    private Date fecha;
    private int fichasGanadas;
    private String cartasGanadoras;
    private String contraJugadores;
    private String tipoManoGanadora;
}