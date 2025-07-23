package com.pokeronline.moderacion.service;

import com.pokeronline.moderacion.model.MotivoSancion;
import com.pokeronline.moderacion.model.Sancion;
import com.pokeronline.moderacion.model.TipoSancion;
import com.pokeronline.moderacion.repository.SancionRepository;
import com.pokeronline.model.User;
import com.pokeronline.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ModeracionAdminService {

    private final UserRepository userRepository;
    private final SancionRepository sancionRepository;

    @Transactional
    public void aplicarSancionManual(Long userId, TipoSancion tipo, MotivoSancion motivo, String descripcion, int diasDuracion) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + userId));

        Date fechaInicio = new Date();
        Date fechaFin = null;

        // Si es suspensión temporal, calcula la fecha de fin
        if (tipo == TipoSancion.SUSPENSION_TEMPORAL) {
            fechaFin = new Date(System.currentTimeMillis() + (long) diasDuracion * 24 * 60 * 60 * 1000);
        }

        Sancion sancion = Sancion.builder()
                .user(user)
                .tipo(tipo)
                .motivo(motivo)
                .descripcion(descripcion)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .build();

        sancionRepository.save(sancion);

        // Aquí podrías ejecutar otras consecuencias, por ejemplo:
        if (tipo == TipoSancion.BLOQUEO_CUENTA) {
            user.setBloqueado(true);
            userRepository.save(user);
        }

        if (tipo == TipoSancion.PROHIBICION_CHAT) {
            user.setChatBloqueado(true); // Suponiendo que tienes este campo
            userRepository.save(user);
        }

        // Notificación al sistema o logs
        System.out.printf("Sanción aplicada al usuario %s: %s (%s)%n", user.getUsername(), tipo, motivo);
    }
}