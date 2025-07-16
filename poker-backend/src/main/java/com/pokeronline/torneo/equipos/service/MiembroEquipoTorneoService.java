package com.pokeronline.torneo.equipos.service;

import com.pokeronline.model.User;
import com.pokeronline.torneo.equipos.model.EquipoTorneo;
import com.pokeronline.torneo.equipos.model.MiembroEquipoTorneo;
import com.pokeronline.torneo.equipos.repository.EquipoTorneoRepository;
import com.pokeronline.torneo.equipos.repository.MiembroEquipoTorneoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MiembroEquipoTorneoService {

    private final MiembroEquipoTorneoRepository miembroRepo;
    private final EquipoTorneoRepository equipoRepo;

    public MiembroEquipoTorneo anadirMiembro(EquipoTorneo equipo, User user) {
        MiembroEquipoTorneo miembro = MiembroEquipoTorneo.builder()
                .equipo(equipo)
                .user(user)
                .build();

        return miembroRepo.save(miembro);
    }

    public List<MiembroEquipoTorneo> obtenerMiembrosEquipo(Long equipoId) {
        EquipoTorneo equipo = equipoRepo.findById(equipoId).orElseThrow();
        return miembroRepo.findByEquipo(equipo);
    }

    public void eliminarMiembro(Long miembroId) {
        miembroRepo.deleteById(miembroId);
    }

    public void eliminarTodosLosMiembrosDeEquipo(Long equipoId) {
        EquipoTorneo equipo = equipoRepo.findById(equipoId).orElseThrow();
        List<MiembroEquipoTorneo> miembros = miembroRepo.findByEquipo(equipo);
        miembroRepo.deleteAll(miembros);
    }
}
