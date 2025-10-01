package com.pokeronline.amigos.service;

import com.pokeronline.amigos.dto.LimitesTransferenciaDTO;
import com.pokeronline.amigos.dto.TransferenciaFichasDTO;
import com.pokeronline.amigos.dto.TransferirFichasDTO;
import com.pokeronline.amigos.model.Amistad;
import com.pokeronline.amigos.model.EstadoTransferencia;
import com.pokeronline.amigos.model.TransferenciaFichas;
import com.pokeronline.amigos.repository.AmistadRepository;
import com.pokeronline.amigos.repository.TransferenciaFichasRepository;
import com.pokeronline.exception.ResourceNotFoundException;
import com.pokeronline.model.User;
import com.pokeronline.notificacion.TipoNotificacion;
import com.pokeronline.repository.UserRepository;
import com.pokeronline.websocket.WebSocketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * Transferencia de fichas entre amigos (con límites, anti-fraude básico y comisión).
 */
@Service
@RequiredArgsConstructor
public class TransferenciaFichasService {

    private final TransferenciaFichasRepository transferenciaRepository;
    private final UserRepository userRepository;
    private final AmistadRepository amistadRepository;
    private final NotificacionService notificacionService; // opcional
    private final WebSocketService webSocketService;
    private final AuditoriaService auditoriaService;       // opcional (si no lo tienes aún, deja en comentario)

    private static final long TRANSFERENCIA_MINIMA = 100L;
    private static final long TRANSFERENCIA_MAXIMA = 10_000L;
    private static final long LIMITE_DIARIO = 50_000L;
    private static final int MAX_TRANSFERENCIAS_DIARIAS = 10;
    private static final double COMISION = 0.10; // 10%
    private static final int DIAS_MINIMOS_AMISTAD = 7;

    @Transactional
    public TransferenciaFichasDTO transferirFichas(Long remitenteId, TransferirFichasDTO dto) {
        // Validaciones
        validarAmigos(remitenteId, dto.getDestinatarioId());
        validarAntiguedadAmistad(remitenteId, dto.getDestinatarioId());
        validarCantidad(dto.getCantidad());
        validarLimiteDiario(remitenteId, dto.getCantidad());
        validarCooldown(remitenteId);

        User remitente = userRepository.findById(remitenteId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario remitente no encontrado"));
        User destinatario = userRepository.findById(dto.getDestinatarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario destinatario no encontrado"));

        long comision = Math.round(dto.getCantidad() * COMISION);
        long totalNecesario = dto.getCantidad() + comision;

        if (remitente.getFichas() < totalNecesario) {
            throw new IllegalArgumentException(
                    "Fichas insuficientes. Necesitas " + totalNecesario +
                            " (" + dto.getCantidad() + " + " + comision + " de comisión)"
            );
        }

        // Realizar transferencia
        remitente.setFichas((int) (remitente.getFichas() - totalNecesario));
        destinatario.setFichas((int) (destinatario.getFichas() + dto.getCantidad()));

        userRepository.save(remitente);
        userRepository.save(destinatario);

        // Registrar
        TransferenciaFichas transferencia = TransferenciaFichas.builder()
                .remitente(remitente)
                .destinatario(destinatario)
                .cantidad(dto.getCantidad())
                .mensaje(dto.getMensaje())
                .esRegalo(Boolean.TRUE.equals(dto.getEsRegalo()))
                .fecha(LocalDateTime.now())
                .estado(EstadoTransferencia.COMPLETADA)
                .feeAplicada(comision)
                .build();

        transferencia = transferenciaRepository.save(transferencia);

        // Auditoría (si existe)
        if (auditoriaService != null) {
            auditoriaService.registrarTransferencia(transferencia, comision);
        }

        // Notificaciones
        if (notificacionService != null) {
            notificacionService.enviarNotificacion(
                    destinatario.getId(),
                    TipoNotificacion.TRANSFERENCIA_RECIBIDA,
                    "Fichas recibidas",
                    remitente.getUsername() + " te envió " + dto.getCantidad() + " fichas",
                    Map.of("transferenciaId", transferencia.getId())
            );
        }

        // WebSocket
        TransferenciaFichasDTO out = TransferenciaFichasDTO.fromEntity(transferencia);
        webSocketService.enviarMensajeUsuario(
                destinatario.getId(),
                "/queue/amigos/transferencias",
                out
        );

        return out;
    }

    public LimitesTransferenciaDTO obtenerLimites(Long userId) {
        long usadoHoy = transferenciaRepository.sumarTransferenciasHoy(userId);
        int cantidadHoy = transferenciaRepository.contarTransferenciasHoy(userId);

        return LimitesTransferenciaDTO.builder()
                .limiteDiario(LIMITE_DIARIO)
                .usadoHoy(usadoHoy)
                .restanteHoy(Math.max(0, LIMITE_DIARIO - usadoHoy))
                .limitePorTransferencia(TRANSFERENCIA_MAXIMA)
                .transferenciaMinima(TRANSFERENCIA_MINIMA)
                .transferenciasRealizadasHoy(cantidadHoy)
                .transferenciasRestantesHoy(Math.max(0, MAX_TRANSFERENCIAS_DIARIAS - cantidadHoy))
                .comisionPorcentaje(COMISION * 100)
                .build();
    }

    // ==== Validaciones helper ====

    private void validarAmigos(Long u1, Long u2) {
        if (!amistadRepository.existeAmistad(u1, u2)) {
            throw new IllegalArgumentException("Solo puedes transferir fichas a tus amigos");
        }
    }

    private void validarAntiguedadAmistad(Long u1, Long u2) {
        Amistad amistad = amistadRepository.findByUsuarios(u1, u2)
                .orElseThrow(() -> new ResourceNotFoundException("Amistad no encontrada"));

        long dias = ChronoUnit.DAYS.between(amistad.getFechaAmistad(), LocalDateTime.now());
        if (dias < DIAS_MINIMOS_AMISTAD) {
            throw new IllegalArgumentException("Deben ser amigos por al menos " + DIAS_MINIMOS_AMISTAD + " días para transferir fichas");
        }
    }

    private void validarCantidad(long cantidad) {
        if (cantidad < TRANSFERENCIA_MINIMA) {
            throw new IllegalArgumentException("La cantidad mínima es " + TRANSFERENCIA_MINIMA);
        }
        if (cantidad > TRANSFERENCIA_MAXIMA) {
            throw new IllegalArgumentException("La cantidad máxima es " + TRANSFERENCIA_MAXIMA);
        }
    }

    private void validarLimiteDiario(Long userId, long cantidad) {
        long usadoHoy = transferenciaRepository.sumarTransferenciasHoy(userId);
        if (usadoHoy + cantidad > LIMITE_DIARIO) {
            long restante = Math.max(0, LIMITE_DIARIO - usadoHoy);
            throw new IllegalArgumentException("Límite diario excedido. Puedes transferir " + restante + " fichas más hoy");
        }

        int cantidadHoy = transferenciaRepository.contarTransferenciasHoy(userId);
        if (cantidadHoy >= MAX_TRANSFERENCIAS_DIARIAS) {
            throw new IllegalArgumentException("Has alcanzado el máximo de transferencias diarias");
        }
    }

    private void validarCooldown(Long userId) {
        LocalDateTime ultima = transferenciaRepository.findUltimaTransferencia(userId);
        if (ultima != null) {
            long minutos = ChronoUnit.MINUTES.between(ultima, LocalDateTime.now());
            if (minutos < 5) {
                throw new IllegalArgumentException("Debes esperar " + (5 - minutos) + " minutos antes de otra transferencia");
            }
        }
    }
}