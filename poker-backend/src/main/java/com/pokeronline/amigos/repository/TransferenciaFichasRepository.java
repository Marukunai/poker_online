package com.pokeronline.amigos.repository;

import com.pokeronline.amigos.model.TransferenciaFichas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface TransferenciaFichasRepository extends JpaRepository<TransferenciaFichas, Long> {

    @Query(value = """
            SELECT COALESCE(SUM(t.cantidad),0)
            FROM transferencias_fichas t
            WHERE t.remitente_id = :userId
              AND DATE(t.fecha) = CURRENT_DATE
              AND t.estado = 'COMPLETADA'
            """, nativeQuery = true)
    long sumarTransferenciasHoy(Long userId);

    @Query(value = """
            SELECT COUNT(*)
            FROM transferencias_fichas t
            WHERE t.remitente_id = :userId
              AND DATE(t.fecha) = CURRENT_DATE
              AND t.estado = 'COMPLETADA'
            """, nativeQuery = true)
    int contarTransferenciasHoy(Long userId);

    @Query(value = """
            SELECT MAX(t.fecha)
            FROM transferencias_fichas t
            WHERE t.remitente_id = :userId
            """, nativeQuery = true)
    LocalDateTime findUltimaTransferencia(Long userId);
}