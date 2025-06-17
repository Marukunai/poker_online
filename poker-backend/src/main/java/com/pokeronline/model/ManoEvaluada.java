package com.pokeronline.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManoEvaluada {

    private User user;

    private ManoTipo tipo; // Tipo de mano (póker, color, etc.)

    private List<String> cartasGanadoras; // Las 5 cartas que forman la mejor mano

    private int fuerza; // Valor numérico para comparar (ej: ESCALERA_REAL = 10, CARTA_ALTA = 1)

}