package com.pokeronline.torneo.repository;

import com.pokeronline.estadisticas.dto.RankingUsuarioDTO;
import com.pokeronline.model.User;
import com.pokeronline.torneo.model.ParticipanteTorneo;
import com.pokeronline.torneo.model.Torneo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipanteTorneoRepository extends JpaRepository<ParticipanteTorneo, Long> {
    List<ParticipanteTorneo> findByTorneo(Torneo torneo);
    List<ParticipanteTorneo> findByUser(User user);
    Optional<ParticipanteTorneo> findByTorneoAndUser(Torneo torneo, User user);

    @Query("""
        SELECT new com.pokeronline.estadisticas.RankingUsuarioDTO(
            pt.user.id,
            pt.user.username,
            SUM(pt.puntos),
            SUM(CASE WHEN pt.posicion = 1 THEN 1 ELSE 0 END),
            0
        )
        FROM ParticipanteTorneo pt
        GROUP BY pt.user.id, pt.user.username
        ORDER BY SUM(pt.puntos) DESC
    """)
    List<RankingUsuarioDTO> obtenerRankingGlobal();

    @Query("""
        SELECT new com.pokeronline.estadisticas.RankingUsuarioDTO(
            pt.user.id,
            pt.user.username,
            SUM(pt.puntos),
            SUM(CASE WHEN pt.posicion = 1 THEN 1 ELSE 0 END),
            0
        )
        FROM ParticipanteTorneo pt
        WHERE FUNCTION('YEAR', pt.torneo.fechaInicio) = :year
          AND FUNCTION('MONTH', pt.torneo.fechaInicio) = :mes
        GROUP BY pt.user.id, pt.user.username
        ORDER BY SUM(pt.puntos) DESC
    """)
    List<RankingUsuarioDTO> obtenerRankingMensual(@Param("year") int year, @Param("mes") int mes);

}