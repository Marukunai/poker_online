package com.pokeronline.amigos.repository;

import com.pokeronline.amigos.model.EstadoConexion;
import com.pokeronline.amigos.model.EstadoPresencia;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface EstadoPresenciaRepository extends JpaRepository<EstadoPresencia, Long> {

    @Modifying(clearAutomatically = false, flushAutomatically = false)
    @Query("""
        update EstadoPresencia e
           set e.estado = :estado,
               e.detalleEstado = :detalle,
               e.mesaId = :mesaId,
               e.torneoId = :torneoId,
               e.aceptaInvitaciones = :acepta,
               e.ultimaActividad = :ts
         where e.user.id = :userId
    """)
    int updateSnapshot(@Param("userId") Long userId,
                       @Param("estado") EstadoConexion estado,
                       @Param("detalle") String detalle,
                       @Param("mesaId") Long mesaId,
                       @Param("torneoId") Long torneoId,
                       @Param("acepta") Boolean acepta,
                       @Param("ts") LocalDateTime ts);

    @Modifying(clearAutomatically = false, flushAutomatically = false)
    @Query("update EstadoPresencia e set e.ultimaActividad = :ts where e.user.id = :userId")
    void touch(@Param("userId") Long userId, @Param("ts") LocalDateTime ts);
}