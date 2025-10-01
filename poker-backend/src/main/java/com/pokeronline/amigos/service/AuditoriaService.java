package com.pokeronline.amigos.service;

import com.pokeronline.amigos.model.TransferenciaFichas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaService {

    // Si en el futuro creas una tabla de auditoría:
    // private final AuditoriaRepository auditoriaRepository;

    /**
     * Registra una transferencia a efectos de auditoría (log + hook para persistencia futura).
     */
    public void registrarTransferencia(TransferenciaFichas transferencia, long comision) {
        log.info("[AUDITORIA] Transferencia id={} remitente={} destinatario={} cantidad={} comision={}",
                transferencia.getId(),
                transferencia.getRemitente().getId(),
                transferencia.getDestinatario().getId(),
                transferencia.getCantidad(),
                comision
        );

        // Ejemplo de persistencia futura:
        // Auditoria aud = Auditoria.builder()
        //     .tipo("TRANSFERENCIA_FICHAS")
        //     .referenciaId(transferencia.getId())
        //     .usuarioId(transferencia.getRemitente().getId())
        //     .detalle("Comisión=" + comision)
        //     .fecha(LocalDateTime.now())
        //     .build();
        // auditoriaRepository.save(aud);
    }
}