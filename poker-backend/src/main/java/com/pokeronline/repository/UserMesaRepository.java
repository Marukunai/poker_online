package com.pokeronline.repository;

import com.pokeronline.model.UserMesa;
import com.pokeronline.model.User;
import com.pokeronline.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserMesaRepository extends JpaRepository<UserMesa, Long> {
    List<UserMesa> findByMesa(Mesa mesa);
    List<UserMesa> findByUser(User user);
    Optional<UserMesa> findByUserAndMesa(User user, Mesa mesa);
}
