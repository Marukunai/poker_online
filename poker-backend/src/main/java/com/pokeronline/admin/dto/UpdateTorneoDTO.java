package com.pokeronline.admin.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UpdateTorneoDTO {
    private String nombre;
    private Date fechaInicio;
    private Date fechaFin;
    private boolean eliminacionDirecta;
    private int fichasIniciales;
}