package com.pokeronline.amigos.repository;

import com.pokeronline.amigos.model.MensajePrivado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensajePrivadoRepository extends JpaRepository<MensajePrivado, Long> {

    @Query("""
           select m
           from MensajePrivado m
           where (
                 (m.remitente.id = :userId and m.destinatario.id = :amigoId and m.eliminadoPorRemitente = false)
              or (m.remitente.id = :amigoId and m.destinatario.id = :userId and m.eliminadoPorDestinatario = false)
           )
           order by m.fechaEnvio asc
           """)
    Page<MensajePrivado> findConversacion(Long userId, Long amigoId, Pageable pageable);

    @Query("""
           select m
           from MensajePrivado m
           where m.destinatario.id = :userId
             and m.remitente.id = :remitenteId
             and m.leido = false
           """)
    List<MensajePrivado> findNoLeidosDeRemitente(Long userId, Long remitenteId);

    @Query("""
           select count(m)
           from MensajePrivado m
           where m.destinatario.id = :userId
             and m.leido = false
           """)
    int countNoLeidos(Long userId);

    // MySQL nativo para ventana de 1 minuto
    @Query(value = """
            SELECT COUNT(*) FROM mensajes_privados
            WHERE remitente_id = :userId
              AND fecha_envio >= (NOW() - INTERVAL 1 MINUTE)
            """, nativeQuery = true)
    long contarMensajesUltimoMinuto(@Param("userId") Long userId);

    // Marca la conversación como eliminada para 'userId' (no borra físicamente)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE mensajes_privados
            SET
              eliminado_por_remitente   = (eliminado_por_remitente   OR remitente_id   = :userId),
              eliminado_por_destinatario= (eliminado_por_destinatario OR destinatario_id = :userId)
            WHERE (remitente_id IN (:userId, :amigoId))
              AND (destinatario_id IN (:userId, :amigoId))
            """, nativeQuery = true)

    void eliminarConversacion(@Param("userId") Long userId, @Param("amigoId") Long amigoId);

    @Query("""
           select (count(m) > 0) from MensajePrivado m
           where (m.remitente.id = :a and m.destinatario.id = :b)
              or (m.remitente.id = :b and m.destinatario.id = :a)
           """)
    boolean existsAnyBetween(Long a, Long b);
}