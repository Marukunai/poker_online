package com.pokeronline.moderacion.repository;

import com.pokeronline.moderacion.model.MotivoSancion;
import com.pokeronline.moderacion.model.Sancion;
import com.pokeronline.model.User;
import com.pokeronline.moderacion.model.TipoSancion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SancionRepository extends JpaRepository<Sancion, Long> {
    List<Sancion> findByUser(User user);
    List<Sancion> findByUser_Id(Long userId);
    List<Sancion> findByActivoTrue();
    List<Sancion> findByUser_IdAndMotivoInAndTipo(
            Long userId,
            List<MotivoSancion> motivos,
            TipoSancion tipo
    );}