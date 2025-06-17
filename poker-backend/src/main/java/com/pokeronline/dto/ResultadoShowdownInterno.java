package com.pokeronline.dto;

import com.pokeronline.model.ManoTipo;
import com.pokeronline.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ResultadoShowdownInterno {
    private List<User> ganadores;
    private ManoTipo tipoManoGanadora;
    private List<String> cartasGanadoras;
}