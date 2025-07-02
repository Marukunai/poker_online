package com.pokeronline.repository;

import com.pokeronline.model.EspectadorMesa;
import com.pokeronline.model.Mesa;
import com.pokeronline.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EspectadorMesaRepository extends JpaRepository<EspectadorMesa, Long> {
    List<EspectadorMesa> findByMesa(Mesa mesa);
    Optional<EspectadorMesa> findByMesaAndUser(Mesa mesa, User user);
    void deleteByMesaAndUser(Mesa mesa, User user);
}
