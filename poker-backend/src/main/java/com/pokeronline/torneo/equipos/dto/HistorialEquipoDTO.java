package com.pokeronline.torneo.equipos.dto;

import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistorialEquipoDTO {
    private Long torneoId;
    private String nombreTorneo;
    private Date fechaInicio;
    private int posicion;
    private int puntosObtenidos;
    private boolean ganado;
}