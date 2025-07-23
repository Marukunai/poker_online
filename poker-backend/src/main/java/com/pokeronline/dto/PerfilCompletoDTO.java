package com.pokeronline.dto;

import com.pokeronline.estadisticas.dto.TorneoHistorialDTO;
import com.pokeronline.logros.dto.LogroUsuarioDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilCompletoDTO {
    private UserDTO user;
    private List<LogroUsuarioDTO> logros;
    private List<TorneoHistorialDTO> historialTorneos;
    private List<HistorialManoDTO> ultimasPartidas;
}