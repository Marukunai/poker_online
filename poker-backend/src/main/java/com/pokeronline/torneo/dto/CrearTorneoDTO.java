package com.pokeronline.torneo.dto;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearTorneoDTO {
    private String nombre;
    private int buyIn;
    private int fichasIniciales;
    private int premioTotal;
    private int maxParticipantes;
    private boolean eliminacionDirecta;
    private Date fechaInicio;
}