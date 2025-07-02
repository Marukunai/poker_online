package com.pokeronline.torneo.repository;

import com.pokeronline.torneo.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TorneoRepository extends JpaRepository<Torneo, Long> {
    Optional<Torneo> findByNombre(String nombre);
}