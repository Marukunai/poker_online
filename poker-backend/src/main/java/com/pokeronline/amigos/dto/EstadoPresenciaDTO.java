package com.pokeronline.amigos.dto;

import com.pokeronline.amigos.model.EstadoConexion;
import lombok.Data;

@Data
public class EstadoPresenciaDTO {
    private EstadoConexion estado;
    private String detalleEstado;
    private Long mesaId;
    private Long torneoId;
}