package com.pokeronline.torneo.repository;

import com.pokeronline.torneo.model.EsperaTorneo;
import com.pokeronline.model.User;
import com.pokeronline.torneo.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EsperaTorneoRepository extends JpaRepository<EsperaTorneo, Long> {
    Optional<EsperaTorneo> findByTorneoAndUsuario(Torneo torneo, User usuario);
    List<EsperaTorneo> findByTorneo(Torneo torneo);
    void deleteByTorneo(Torneo torneo);
    long countByTorneo_Id(Long torneoId);
}