package com.pokeronline.moderacion.service;

import com.pokeronline.exception.ResourceNotFoundException;
import com.pokeronline.model.User;
import com.pokeronline.moderacion.model.MotivoSancion;
import com.pokeronline.moderacion.model.Sancion;
import com.pokeronline.moderacion.model.TipoSancion;
import com.pokeronline.moderacion.repository.SancionRepository;
import com.pokeronline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ModeracionService {

    private final SancionRepository sancionRepository;
    private final UserRepository userRepository;

    public void registrarSancion(Long userId, MotivoSancion motivo, TipoSancion tipo, String detalle, Long partidaId, Long torneoId) {
        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Date now = new Date();
        Date fin = switch (tipo) {
            case ADVERTENCIA -> new Date(now.getTime() + TimeUnit.DAYS.toMillis(1));
            case PROHIBICION_CHAT -> new Date(now.getTime() + TimeUnit.DAYS.toMillis(3));
            case SUSPENSION_TEMPORAL -> new Date(now.getTime() + TimeUnit.DAYS.toMillis(7));
            case SUSPENSION_PERMANENTE, BLOQUEO_CUENTA -> null;
            default -> null;
        };

        if (tipo == TipoSancion.PROHIBICION_CHAT) {
            usuario.setChatBloqueado(true);
        }
        if (tipo == TipoSancion.BLOQUEO_CUENTA
                || tipo == TipoSancion.SUSPENSION_TEMPORAL
                || tipo == TipoSancion.SUSPENSION_PERMANENTE) {
            usuario.setBloqueado(true);
        }
        userRepository.save(usuario);

        Sancion s = Sancion.builder()
                .user(usuario)
                .motivo(motivo)
                .tipo(tipo)
                .descripcion(detalle)
                .fechaInicio(now)
                .fechaFin(fin)
                .partidaId(partidaId)
                .torneoId(torneoId)
                .activo(true)
                .build();

        sancionRepository.save(s);
        aplicarSancionesProgresivas(usuario);
    }

    public void aplicarSancionesProgresivas(User user) {
        List<Sancion> historial = sancionRepository.findByUser(user);

        long advertenciasRecientes = historial.stream()
                .filter(s -> s.getTipo() == TipoSancion.ADVERTENCIA)
                .count();

        long suspensiones = historial.stream()
                .filter(s -> s.getTipo() == TipoSancion.SUSPENSION_TEMPORAL || s.getTipo() == TipoSancion.SUSPENSION_PERMANENTE)
                .count();

        // 3 advertencias → Expulsión automática
        if (advertenciasRecientes >= 3 && historial.stream().noneMatch(s -> s.getTipo() == TipoSancion.EXPULSION_PARTIDA)) {
            registrarSancion(user.getId(), MotivoSancion.CONDUCTA_ANTIDEPORTIVA, TipoSancion.EXPULSION_PARTIDA,
                    "Expulsado automáticamente por acumular 3 advertencias", null, null);
        }

        // 2 expulsiones + advertencias → suspensión temporal
        long expulsiones = historial.stream()
                .filter(s -> s.getTipo() == TipoSancion.EXPULSION_PARTIDA || s.getTipo() == TipoSancion.EXPULSION_TORNEO)
                .count();

        if (expulsiones >= 2 && suspensiones == 0) {
            registrarSancion(user.getId(), MotivoSancion.REITERACION_INFRACCIONES, TipoSancion.SUSPENSION_TEMPORAL,
                    "Suspensión por reincidencia de conductas inapropiadas", null, null);
        }

        // Muchas sanciones → suspensión permanente
        if (suspensiones >= 2) {
            registrarSancion(user.getId(), MotivoSancion.INFRACCIONES_GRAVES, TipoSancion.SUSPENSION_PERMANENTE,
                    "Suspensión permanente por conducta reiterada o grave", null, null);
        }
    }

    public long contarAdvertenciasChat(Long userId) {
        List<Sancion> sanciones = sancionRepository.findByUser_IdAndMotivoInAndTipo(
                userId,
                List.of(MotivoSancion.LENGUAJE_OBSCENO, MotivoSancion.ABUSO_DEL_CHAT),
                TipoSancion.ADVERTENCIA
        );

        return sanciones.stream()
                .filter(s -> s.getFechaFin() == null || s.getFechaFin().after(new Date()))
                .count();
    }

    public void evaluarProhibicionChat(Long userId, Long mesaId) {
        long advertencias = contarAdvertenciasChat(userId);
        if (advertencias >= 3) {
            registrarSancion(
                    userId,
                    MotivoSancion.ABUSO_DEL_CHAT,
                    TipoSancion.PROHIBICION_CHAT,
                    "Has recibido 3 advertencias por mal uso del chat",
                    mesaId,
                    null
            );

            // Bloquear el chat del usuario
            User user = userRepository.findById(userId).orElseThrow();
            user.setChatBloqueado(true);
            userRepository.save(user);
        }
    }

    public void sancionarAutomaticamente(User user, String motivoTexto) {
        MotivoSancion motivo;

        switch (motivoTexto) {
            case "ABANDONO_REITERADO" -> motivo = MotivoSancion.ABANDONO_REITERADO;
            case "DESCONEXIONES_SOSPECHOSAS" -> motivo = MotivoSancion.DESCONEXIONES_SOSPECHOSAS;
            default -> throw new IllegalArgumentException("Motivo no reconocido: " + motivoTexto);
        }

        Sancion sancion = Sancion.builder()
                .user(user)
                .motivo(motivo)
                .fechaInicio(new Date())
                .fechaFin(null) // Puedes establecer duración automática si quieres
                .descripcion("Sanción automática por: " + motivoTexto)
                .activo(true)
                .build();

        sancionRepository.save(sancion);
    }
}