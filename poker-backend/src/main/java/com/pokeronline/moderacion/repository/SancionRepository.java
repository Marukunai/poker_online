package com.pokeronline.moderacion.repository;

import com.pokeronline.moderacion.model.MotivoSancion;
import com.pokeronline.moderacion.model.Sancion;
import com.pokeronline.model.User;
import com.pokeronline.moderacion.model.TipoSancion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface SancionRepository extends JpaRepository<Sancion, Long> {
    List<Sancion> findByUser(User user);
    List<Sancion> findByUser_Id(Long userId);
    List<Sancion> findByActivoTrue();
    List<Sancion> findByActivoTrueAndFechaFinBefore(Date now);
    boolean existsByUser_IdAndActivoTrueAndTipoIn(Long userId, Collection<TipoSancion> tipos);
    boolean existsByUser_IdAndActivoTrueAndTipo(Long userId, TipoSancion tipo);
    List<Sancion> findByUser_IdAndMotivoInAndTipo(
            Long userId,
            List<MotivoSancion> motivos,
            TipoSancion tipo
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update Sancion s
              set s.activo = false
            where s.activo = true
              and s.fechaFin is not null
              and s.fechaFin < :now
           """)
    int desactivarCaducadas(@Param("now") Date now);
}