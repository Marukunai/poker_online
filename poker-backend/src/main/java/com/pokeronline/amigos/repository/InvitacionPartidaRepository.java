package com.pokeronline.amigos.repository;

import com.pokeronline.amigos.model.EstadoInvitacion;
import com.pokeronline.amigos.model.InvitacionPartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InvitacionPartidaRepository extends JpaRepository<InvitacionPartida, Long> {

    /**
     * Devuelve TODAS las invitaciones en estado PENDIENTE entre dos usuarios
     * (en ambos sentidos).
     */
    @Query("""
           select i
             from InvitacionPartida i
            where i.estado = com.pokeronline.amigos.model.EstadoInvitacion.PENDIENTE
              and (
                    (i.remitente.id = :userId1 and i.destinatario.id = :userId2)
                 or (i.remitente.id = :userId2 and i.destinatario.id = :userId1)
              )
           """)
    List<InvitacionPartida> findPendientesEntreUsuarios(@Param("userId1") Long userId1,
                                                        @Param("userId2") Long userId2);

    /** Helpers opcionales (pueden venirte bien en otras pantallas) */

    List<InvitacionPartida> findByRemitente_IdAndEstado(Long remitenteId, EstadoInvitacion estado);

    List<InvitacionPartida> findByDestinatario_IdAndEstado(Long destinatarioId, EstadoInvitacion estado);

    boolean existsByRemitente_IdAndDestinatario_IdAndEstado(Long remitenteId,
                                                            Long destinatarioId,
                                                            EstadoInvitacion estado);

    /** Para limpiar expiradas si lo necesitas en un job o servicio */
    List<InvitacionPartida> findByEstadoAndFechaExpiracionBefore(EstadoInvitacion estado,
                                                                 LocalDateTime fecha);

    @Query("""
           select (count(i) > 0) from InvitacionPartida i
           where (i.remitente.id = :a and i.destinatario.id = :b)
              or (i.remitente.id = :b and i.destinatario.id = :a)
           """)
    boolean existsAnyBetween(Long a, Long b);
}