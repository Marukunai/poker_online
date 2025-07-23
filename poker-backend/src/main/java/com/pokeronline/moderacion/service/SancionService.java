package com.pokeronline.moderacion.service;

import com.pokeronline.exception.ResourceNotFoundException;
import com.pokeronline.model.User;
import com.pokeronline.moderacion.dto.CrearSancionDTO;
import com.pokeronline.moderacion.dto.SancionDTO;
import com.pokeronline.moderacion.model.Sancion;
import com.pokeronline.moderacion.repository.SancionRepository;
import com.pokeronline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SancionService {

    private final SancionRepository sancionRepository;
    private final UserRepository userRepository;

    public SancionDTO asignarSancion(CrearSancionDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Sancion sancion = Sancion.builder()
                .user(user)
                .tipo(dto.getTipo())
                .motivo(dto.getMotivo())
                .descripcion(dto.getDescripcion())
                .fechaInicio(new Date())
                .fechaFin(dto.getFechaFin())
                .activo(true)
                .build();

        sancionRepository.save(sancion);

        return toDTO(sancion);
    }

    public List<SancionDTO> obtenerSancionesUsuario(Long userId) {
        return sancionRepository.findByUsuarioId(userId)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void desactivarSancion(Long sancionId) {
        Sancion sancion = sancionRepository.findById(sancionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sanción no encontrada"));

        sancion.setActivo(false);
        sancionRepository.save(sancion);
    }

    private SancionDTO toDTO(Sancion s) {
        return SancionDTO.builder()
                .id(s.getId())
                .userId(s.getUser().getId())
                .tipo(s.getTipo())
                .motivo(s.getMotivo())
                .descripcion(s.getDescripcion())
                .fechaInicio(s.getFechaInicio())
                .fechaFin(s.getFechaFin())
                .activo(s.isActivo())
                .build();
    }
}
