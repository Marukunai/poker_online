package com.pokeronline.estadisticas;

import com.pokeronline.estadisticas.dto.EstadisticasUsuarioDTO;
import com.pokeronline.estadisticas.dto.ProgresoMensualDTO;
import com.pokeronline.estadisticas.dto.ProgresoMensualPartidasDTO;
import com.pokeronline.estadisticas.dto.RankingUsuarioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
public class EstadisticasController {

    private final EstadisticasService estadisticasService;

    @GetMapping("/{userId}")
    public EstadisticasUsuarioDTO obtenerEstadisticas(@PathVariable Long userId) {
        return estadisticasService.obtenerEstadisticasUsuario(userId);
    }

    @GetMapping("/{userId}/progreso-mensual-torneos")
    public List<ProgresoMensualDTO> obtenerProgresoMensualTorneos(@PathVariable Long userId) {
        return estadisticasService.obtenerProgresoMensualTorneos(userId);
    }

    @GetMapping("/{userId}/progreso-mensual-partidas")
    public List<ProgresoMensualPartidasDTO> obtenerProgresoMensualPartidas(@PathVariable Long userId) {
        return estadisticasService.obtenerProgresoMensualPartidas(userId);
    }

    @GetMapping("/ranking/global")
    public List<RankingUsuarioDTO> rankingGlobal() {
        return estadisticasService.obtenerRankingGlobal();
    }

    @GetMapping("/ranking/mensual")
    public List<RankingUsuarioDTO> rankingMensual(@RequestParam int year, @RequestParam int mes) {
        return estadisticasService.obtenerRankingMensual(year, mes);
    }
}