package com.pokeronline.moderacion.service;

import com.pokeronline.exception.AlreadyInactiveException;
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
                // Sanciones temporales deben traer fechaFin
                if (dto.getFechaFin() == null) {
                    throw new IllegalArgumentException("Las sanciones temporales requieren fechaFin.");
                }
            }
        }

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
