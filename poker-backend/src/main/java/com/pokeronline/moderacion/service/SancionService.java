package com.pokeronline.moderacion.service;

import com.pokeronline.exception.AlreadyInactiveException;
import com.pokeronline.exception.ActiveSanctionExistsException;
import com.pokeronline.exception.ResourceNotFoundException;
import com.pokeronline.model.User;
import com.pokeronline.moderacion.dto.CrearSancionDTO;
import com.pokeronline.moderacion.dto.SancionDTO;
import com.pokeronline.moderacion.model.Sancion;
import com.pokeronline.moderacion.model.TipoSancion;
import com.pokeronline.moderacion.repository.SancionRepository;
import com.pokeronline.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SancionService {

    private final SancionRepository sancionRepository;
    private final UserRepository userRepository;

    public SancionDTO asignarSancion(CrearSancionDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Validaciones de coherencia
        switch (dto.getTipo()) {
            case SUSPENSION_PERMANENTE, BLOQUEO_CUENTA -> {
                if (dto.getFechaFin() != null) {
                    throw new IllegalArgumentException("Las sanciones permanentes no deben tener fechaFin.");
                }
            }
            default -> {
                if (dto.getFechaFin() == null) {
                    throw new IllegalArgumentException("Las sanciones temporales requieren fechaFin.");
                }
            }
        }

        // Anti-duplicado: PROHIBICION_CHAT
        if (dto.getTipo() == TipoSancion.PROHIBICION_CHAT &&
                sancionRepository.existsByUser_IdAndActivoTrueAndTipo(dto.getUserId(), TipoSancion.PROHIBICION_CHAT)) {
            throw new ActiveSanctionExistsException("El usuario ya tiene una PROHIBICION_CHAT activa.");
        }

        // Anti-duplicado: bloqueos/suspensiones
        if (dto.getTipo() == TipoSancion.BLOQUEO_CUENTA
                || dto.getTipo() == TipoSancion.SUSPENSION_TEMPORAL
                || dto.getTipo() == TipoSancion.SUSPENSION_PERMANENTE) {
            boolean yaBloqueado = sancionRepository.existsByUser_IdAndActivoTrueAndTipoIn(
                    dto.getUserId(),
                    List.of(TipoSancion.BLOQUEO_CUENTA, TipoSancion.SUSPENSION_TEMPORAL, TipoSancion.SUSPENSION_PERMANENTE)
            );
            if (yaBloqueado) {
                throw new ActiveSanctionExistsException("El usuario ya tiene una sanción de bloqueo/suspensión activa.");
            }
        }

        // Actualizar flags de User
        if (dto.getTipo() == TipoSancion.PROHIBICION_CHAT) {
            user.setChatBloqueado(true);
        }
        if (dto.getTipo() == TipoSancion.BLOQUEO_CUENTA
                || dto.getTipo() == TipoSancion.SUSPENSION_TEMPORAL
                || dto.getTipo() == TipoSancion.SUSPENSION_PERMANENTE) {
            user.setBloqueado(true);
        }
        userRepository.save(user);

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
        return sancionRepository.findByUser_Id(userId)
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Sancion> listarCaducadasPendientes() {
        return sancionRepository.findByActivoTrueAndFechaFinBefore(new Date());
    }

    public void desactivarSancion(Long sancionId) {
        Sancion sancion = sancionRepository.findById(sancionId)
                .orElseThrow(() -> new ResourceNotFoundException("Sanción no encontrada"));

        if (!sancion.isActivo()) {
            throw new AlreadyInactiveException("La sanción ya está desactivada");
        }

        sancion.setActivo(false);
        sancionRepository.save(sancion);
    }

    @Transactional
    public int desactivarCaducadasConDetalle() {
        List<Sancion> caducadas = sancionRepository.findByActivoTrueAndFechaFinBefore(new Date());
        if (caducadas.isEmpty()) return 0;

        // Desactivar sanciones vencidas
        caducadas.forEach(s -> s.setActivo(false));
        sancionRepository.saveAll(caducadas);

        // Revertir flags en User si ya no quedan sanciones activas del mismo tipo
        Map<User, List<Sancion>> porUsuario = caducadas.stream().collect(Collectors.groupingBy(Sancion::getUser));
        for (var entry : porUsuario.entrySet()) {
            User u = entry.getKey();

            boolean quedaChatBan = sancionRepository.existsByUser_IdAndActivoTrueAndTipoIn(
                    u.getId(), List.of(TipoSancion.PROHIBICION_CHAT));
            if (!quedaChatBan) u.setChatBloqueado(false);

            boolean quedaBloqueo = sancionRepository.existsByUser_IdAndActivoTrueAndTipoIn(
                    u.getId(), List.of(TipoSancion.BLOQUEO_CUENTA, TipoSancion.SUSPENSION_TEMPORAL));
            if (!quedaBloqueo) u.setBloqueado(false);

            userRepository.save(u);
        }
        return caducadas.size();
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