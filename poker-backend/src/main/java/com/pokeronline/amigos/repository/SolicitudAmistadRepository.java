package com.pokeronline.amigos.repository;

import com.pokeronline.amigos.model.EstadoSolicitud;
import com.pokeronline.amigos.model.SolicitudAmistad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SolicitudAmistadRepository extends JpaRepository<SolicitudAmistad, Long> {

    @Query("""
           select case when count(s)>0 then true else false end
           from SolicitudAmistad s
           where s.remitente.id = :remitenteId
             and s.destinatario.id = :destinatarioId
             and s.estado = com.pokeronline.amigos.model.EstadoSolicitud.PENDIENTE
           """)
    boolean existePendiente(Long remitenteId, Long destinatarioId);

    @Query("""
           select count(s)
           from SolicitudAmistad s
           where s.remitente.id = :remitenteId
             and function('DATE', s.fechaEnvio) = CURRENT_DATE
           """)
    long contarSolicitudesHoy(Long remitenteId);
}
