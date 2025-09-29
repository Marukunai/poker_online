package com.pokeronline.logros.service;

import com.pokeronline.exception.AlreadyHasAchievementException;
import com.pokeronline.exception.ResourceNotFoundException;
import com.pokeronline.logros.dto.LogroDTO;
import com.pokeronline.logros.model.Logro;
import com.pokeronline.logros.model.LogroUsuario;
import com.pokeronline.logros.repository.LogroRepository;
import com.pokeronline.logros.repository.LogroUsuarioRepository;
import com.pokeronline.model.User;
import com.pokeronline.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogroService {

    private final LogroRepository logroRepository;
    private final LogroUsuarioRepository logroUsuarioRepository;
    private final UserRepository userRepository;

    public List<LogroDTO> obtenerTodosLosLogros() {
        return logroRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Otorga un logro a un usuario si aún no lo tiene.
     * Lanza AlreadyHasAchievementException si ya lo tenía.
     * Lanza ResourceNotFoundException si el logro no existe.
     */
    @Transactional
    public void otorgarLogroSiNoTiene(Long userId, String nombreLogro) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("Usuario no encontrado: " + userId)
        );

        Logro logro = logroRepository.findByNombre(nombreLogro);
        if (logro == null) {
            throw new ResourceNotFoundException("Logro no encontrado: " + nombreLogro);
        }

        boolean yaTiene = logroUsuarioRepository.existsByUserAndLogro(user, logro);
        if (yaTiene) {
            throw new AlreadyHasAchievementException(
                    "El usuario ya obtuvo el logro: " + logro.getNombre()
            );
        }

        LogroUsuario lu = LogroUsuario.builder()
                .logro(logro)
                .user(user)
                .fechaObtencion(new Date())
                .build();
        logroUsuarioRepository.save(lu);
    }

    // ================== Mapeos ===================

    private LogroDTO toDTO(Logro logro) {
        return LogroDTO.builder()
                .id(logro.getId())
                .nombre(logro.getNombre())
                .descripcion(logro.getDescripcion())
                .iconoLogro(logro.getIconoLogro())
                .categoria(logro.getCategoria() != null ? logro.getCategoria().name() : null)
                .build();
    }
}