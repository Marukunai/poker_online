package com.pokeronline.repository;

import com.pokeronline.model.HistorialMano;
import com.pokeronline.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialManoRepository extends JpaRepository<HistorialMano, Long> {
    List<HistorialMano> findByJugadorOrderByFechaDesc(User jugador);
}