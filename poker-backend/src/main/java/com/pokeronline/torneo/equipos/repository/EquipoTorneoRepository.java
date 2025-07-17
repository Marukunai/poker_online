package com.pokeronline.torneo.equipos.repository;

import com.pokeronline.torneo.equipos.model.EquipoTorneo;
import com.pokeronline.torneo.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface EquipoTorneoRepository extends JpaRepository<EquipoTorneo, Long> {
    List<EquipoTorneo> findByTorneo(Torneo torneo);
    List<EquipoTorneo> findAllByOrderByPuntosTotalesDesc();
    List<EquipoTorneo> findByTorneo_IdOrderByPuntosTotalesDesc(Long torneoId);
    List<EquipoTorneo> findByTorneo_FechaInicioAfterOrderByPuntosTotalesDesc(Date fecha);
    List<EquipoTorneo> findByTorneo_FechaInicioBetweenOrderByPuntosTotalesDesc(
            Date inicio,
            Date fin
    );
    List<EquipoTorneo> findByNombreAndTorneoIsNotNullOrderByTorneo_FechaInicioDesc(String nombre);
}