package com.pokeronline.repository;

import com.pokeronline.model.Mesa;
import com.pokeronline.model.Turno;
import com.pokeronline.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByMesaOrderByOrdenTurno(Mesa mesa);

    Optional<Turno> findByMesaAndActivoTrue(Mesa mesa);

    Optional<Turno> findByMesaAndUser(Mesa mesa, User user);

    void deleteAllByMesa(Mesa mesa);
}