package com.pokeronline.torneo.repository;

import com.pokeronline.torneo.model.BlindLevel;
import com.pokeronline.torneo.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlindLevelRepository extends JpaRepository<BlindLevel, Long> {
    List<BlindLevel> findByTorneoOrderByNivelAsc(Torneo torneo);
}