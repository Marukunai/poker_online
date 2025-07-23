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

@Service
@RequiredArgsConstructor
public class ModeracionService {

    private final SancionRepository sancionRepository;
    private final UserRepository userRepository;

    public void registrarSancion(Long userId, MotivoSancion motivo, TipoSancion tipo, String detalle, Long partidaId, Long torneoId) {
        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Sancion sancion = Sancion.builder()
                .user(usuario)
                .motivo(motivo)
                .tipo(tipo)
                .descripcion(detalle)
                .fechaInicio(new Date())
                .fechaFin(new Date())
                .partidaId(partidaId)
                .torneoId(torneoId)
                .build();

        sancionRepository.save(sancion);
        aplicarSancionesProgresivas(usuario);
    }

    public void aplicarSancionesProgresivas(User user) {
        List<Sancion> historial = sancionRepository.findByUsuario(user);

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
}