package com.pokeronline.torneo.repository;

import com.pokeronline.torneo.model.Torneo;
import com.pokeronline.torneo.model.TorneoEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TorneoRepository extends JpaRepository<Torneo, Long> {
    Optional<Torneo> findByNombre(String nombre);

    List<Torneo> findByEstado(TorneoEstado torneoEstado);
}