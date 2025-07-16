package com.pokeronline.torneo.equipos.service;

import com.pokeronline.model.User;
import com.pokeronline.torneo.model.Torneo;
import com.pokeronline.torneo.equipos.model.EquipoTorneo;
import com.pokeronline.torneo.repository.TorneoRepository;
import com.pokeronline.torneo.equipos.repository.EquipoTorneoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipoTorneoService {

    private final EquipoTorneoRepository equipoTorneoRepository;
    private final TorneoRepository torneoRepository;

    public EquipoTorneo crearEquipo(Torneo torneo, String nombreEquipo, User capitan) {
        EquipoTorneo equipo = EquipoTorneo.builder()
                .torneo(torneo)
                .nombre(nombreEquipo)
                .capitan(capitan)
                .puntosTotales(0)
                .build();

        return equipoTorneoRepository.save(equipo);
    }

    public List<EquipoTorneo> listarEquiposDeTorneo(Long torneoId) {
        Torneo torneo = torneoRepository.findById(torneoId).orElseThrow();
        return equipoTorneoRepository.findByTorneo(torneo);
    }

    public EquipoTorneo obtenerEquipo(Long equipoId) {
        return equipoTorneoRepository.findById(equipoId).orElseThrow();
    }

    public EquipoTorneo actualizarPuntos(Long equipoId, int puntosSumar) {
        EquipoTorneo equipo = obtenerEquipo(equipoId);
        equipo.setPuntosTotales(equipo.getPuntosTotales() + puntosSumar);
        return equipoTorneoRepository.save(equipo);
    }

    public void eliminarEquipo(Long equipoId) {
        equipoTorneoRepository.deleteById(equipoId);
    }
}
