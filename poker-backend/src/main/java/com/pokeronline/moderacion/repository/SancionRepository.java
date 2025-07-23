package com.pokeronline.moderacion.repository;

import com.pokeronline.moderacion.model.Sancion;
import com.pokeronline.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SancionRepository extends JpaRepository<Sancion, Long> {
    List<Sancion> findByUsuario(User usuario);
    List<Sancion> findByUsuarioId(Long userId);
    List<Sancion> findByActivoTrue();
}