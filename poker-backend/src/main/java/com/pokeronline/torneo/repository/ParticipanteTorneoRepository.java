package com.pokeronline.torneo.repository;

import com.pokeronline.model.User;
import com.pokeronline.torneo.model.ParticipanteTorneo;
import com.pokeronline.torneo.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipanteTorneoRepository extends JpaRepository<ParticipanteTorneo, Long> {
    List<ParticipanteTorneo> findByTorneo(Torneo torneo);
    List<ParticipanteTorneo> findByUser(User user);
    Optional<ParticipanteTorneo> findByTorneoAndUser(Torneo torneo, User user);
}