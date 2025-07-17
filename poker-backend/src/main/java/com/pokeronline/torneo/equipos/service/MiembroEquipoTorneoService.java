package com.pokeronline.torneo.equipos.service;

import com.pokeronline.model.User;
import com.pokeronline.service.UserService;
import com.pokeronline.torneo.equipos.model.EquipoTorneo;
import com.pokeronline.torneo.equipos.model.MiembroEquipoTorneo;
import com.pokeronline.torneo.equipos.repository.EquipoTorneoRepository;
import com.pokeronline.torneo.equipos.repository.MiembroEquipoTorneoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MiembroEquipoTorneoService {

    private final MiembroEquipoTorneoRepository miembroRepo;
    private final EquipoTorneoRepository equipoRepo;
    private final UserService userService;

    public MiembroEquipoTorneo anadirMiembro(EquipoTorneo equipo, User user, Long userIdSolicitante) {
        verificarPermisos(equipo.getId(), userIdSolicitante);

        if (miembroRepo.existsByEquipoAndUser(equipo, user)) {
            throw new RuntimeException("El usuario ya es miembro del equipo");
        }

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

    public void eliminarMiembro(Long miembroId, Long userIdSolicitante) {
        MiembroEquipoTorneo miembro = miembroRepo.findById(miembroId)
                .orElseThrow(() -> new RuntimeException("Miembro no encontrado"));

        verificarPermisos(miembro.getEquipo().getId(), userIdSolicitante);
        miembroRepo.deleteById(miembroId);
    }

    public void eliminarTodosLosMiembrosDeEquipo(Long equipoId, Long userIdSolicitante) {
        verificarPermisos(equipoId, userIdSolicitante);

        EquipoTorneo equipo = equipoRepo.findById(equipoId).orElseThrow();
        List<MiembroEquipoTorneo> miembros = miembroRepo.findByEquipo(equipo);
        miembroRepo.deleteAll(miembros);
    }

    private void verificarPermisos(Long equipoId, Long userIdSolicitante) {
        EquipoTorneo equipo = equipoRepo.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        boolean esCapitan = equipo.getCapitan().getId().equals(userIdSolicitante);
        boolean esAdmin = userService.isAdmin(userIdSolicitante);

        if (!esCapitan && !esAdmin) {
            throw new AccessDeniedException("Solo el capitán o un admin puede modificar los miembros");
        }
    }

    public MiembroEquipoTorneo obtenerMiembro(Long equipoId, Long userId) {
        EquipoTorneo equipo = equipoRepo.findById(equipoId)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));

        User user = userService.getById(userId);

        return miembroRepo.findByEquipoAndUser(equipo, user)
                .orElseThrow(() -> new RuntimeException("El usuario no es miembro del equipo"));
    }
}