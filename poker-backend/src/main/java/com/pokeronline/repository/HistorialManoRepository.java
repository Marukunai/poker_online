package com.pokeronline.repository;

import com.pokeronline.model.HistorialMano;
import com.pokeronline.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Date;

public interface HistorialManoRepository extends JpaRepository<HistorialMano, Long> {

    List<HistorialMano> findByJugador(User jugador);

    List<HistorialMano> findByJugadorOrderByFechaDesc(User jugador);

    int countByJugador(User jugador);

    int countByJugadorAndEmpateFalse(User jugador);

    @Query("SELECT SUM(h.fichasGanadas) FROM HistorialMano h WHERE h.jugador = :jugador")
    Optional<Integer> sumFichasGanadasByJugador(User jugador);

    @Query("SELECT COUNT(h) FROM HistorialMano h " +
            "WHERE h.jugador = :jugador " +
            "AND h.mesa.fichasTemporales = true " +
            "AND EXISTS (SELECT jm FROM UserMesa jm " +
            "WHERE jm.mesa = h.mesa AND jm.user.esIA = true)")
    long contarVictoriasVsBots(User jugador);

    @Query("SELECT h.fecha FROM HistorialMano h WHERE h.jugador = :user AND h.fecha >= :desde")
    List<Date> obtenerDiasConPartida(@Param("user") User user, @Param("desde") Date desde);

    List<HistorialMano> findTop5ByJugadorOrderByFechaDesc(User user);
}