package com.pokeronline.dto;

import com.pokeronline.model.ManoTipo;
import com.pokeronline.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ResultadoShowdownDTO {
    private List<JugadorEnMesaDTO> ganadores;
    private List<JugadorEnMesaDTO> jugadores;
    private String manoGanadoraTipo; // Ej: "ESCALERA", "FULL_HOUSE"
    private List<String> cartasGanadoras;
}

