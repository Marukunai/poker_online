package com.pokeronline.repository;

import com.pokeronline.model.HistorialMano;
import com.pokeronline.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HistorialManoRepository extends JpaRepository<HistorialMano, Long> {

    List<HistorialMano> findByJugador(User jugador);

    List<HistorialMano> findByJugadorOrderByFechaDesc(User jugador);

    int countByJugador(User jugador);

    int countByJugadorAndEmpateFalse(User jugador);

    @Query("SELECT SUM(h.fichasGanadas) FROM Historial_Mano h WHERE h.jugador = :jugador")
    Optional<Integer> sumFichasGanadasByJugador(User jugador);
}