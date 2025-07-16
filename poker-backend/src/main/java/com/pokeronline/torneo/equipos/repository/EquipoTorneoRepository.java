package com.pokeronline.torneo.equipos.repository;

import com.pokeronline.torneo.equipos.model.EquipoTorneo;
import com.pokeronline.torneo.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipoTorneoRepository extends JpaRepository<EquipoTorneo, Long> {
    List<EquipoTorneo> findByTorneo(Torneo torneo);
}