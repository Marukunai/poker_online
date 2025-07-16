package com.pokeronline.torneo.equipos.repository;

import com.pokeronline.model.User;
import com.pokeronline.torneo.equipos.model.MiembroEquipoTorneo;
import com.pokeronline.torneo.equipos.model.EquipoTorneo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MiembroEquipoTorneoRepository extends JpaRepository<MiembroEquipoTorneo, Long> {
    List<MiembroEquipoTorneo> findByEquipo(EquipoTorneo equipo);
    List<MiembroEquipoTorneo> findByUser(User user);
}