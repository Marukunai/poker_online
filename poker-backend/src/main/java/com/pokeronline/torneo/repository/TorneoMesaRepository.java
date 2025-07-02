package com.pokeronline.torneo.repository;

import com.pokeronline.model.Mesa;
import com.pokeronline.torneo.model.Torneo;
import com.pokeronline.torneo.model.TorneoMesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TorneoMesaRepository extends JpaRepository<TorneoMesa, Long> {
    List<TorneoMesa> findByTorneo(Torneo torneo);
    Optional<TorneoMesa> findByTorneoAndMesa(Torneo torneo, Mesa mesa);
    Optional<TorneoMesa> findByMesa(Mesa mesa);
}