package com.pokeronline.repository;

import com.pokeronline.model.AccionPartida;
import com.pokeronline.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccionPartidaRepository extends JpaRepository<AccionPartida, Long> {
    List<AccionPartida> findByMesaOrderByTimestampAsc(Mesa mesa);
}