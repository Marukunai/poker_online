package com.pokeronline.repository;

import com.pokeronline.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MesaRepository extends JpaRepository<Mesa, Long> {
    List<Mesa> findByActivaTrue();
    Optional<Mesa> findByCodigoAcceso(String codigoAcceso);
    List<Mesa> findByPrivada(boolean privada);
}