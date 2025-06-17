package com.pokeronline.dto;

import com.pokeronline.model.Fase;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MesaEstadoDTO {
    private Long mesaId;
    private String nombreMesa;
    private boolean activa;
    private Fase fase;
    private int pot;
    private List<String> comunitarias;
    private List<JugadorMesaCompletoDTO> jugadores;
}
