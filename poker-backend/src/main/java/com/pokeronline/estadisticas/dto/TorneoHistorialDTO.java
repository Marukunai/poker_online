package com.pokeronline.estadisticas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TorneoHistorialDTO {
    private Long torneoId;
    private String nombreTorneo;
    private int posicion;
    private Date fechaInicio;
    private boolean ganado;
    private int puntosObtenidos;
    private boolean enEquipo;
    private String nombreEquipo;
}
