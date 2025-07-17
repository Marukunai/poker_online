package com.pokeronline.torneo.equipos.repository;

import com.pokeronline.model.User;
import com.pokeronline.torneo.equipos.model.MiembroEquipoTorneo;
import com.pokeronline.torneo.equipos.model.EquipoTorneo;
import com.pokeronline.torneo.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MiembroEquipoTorneoRepository extends JpaRepository<MiembroEquipoTorneo, Long> {
    List<MiembroEquipoTorneo> findByEquipo(EquipoTorneo equipo);
    boolean existsByEquipoAndUser(EquipoTorneo equipo, User user);
    Optional<MiembroEquipoTorneo> findByEquipoAndUser(EquipoTorneo equipo, User user);
    boolean existsByUserAndEquipo_Torneo(User user, Torneo torneo);
}