package com.pokeronline.amigos.dto;

import com.pokeronline.amigos.model.EstadoSolicitud;
import com.pokeronline.amigos.model.SolicitudAmistad;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudAmistadDTO {
    private Long id;
    private Long remitenteId;
    private String remitenteUsername;
    private Long destinatarioId;
    private EstadoSolicitud estado;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaRespuesta;
    private String mensaje;

    public static SolicitudAmistadDTO fromEntity(SolicitudAmistad s) {
        return SolicitudAmistadDTO.builder()
                .id(s.getId())
                .remitenteId(s.getRemitente().getId())
                .remitenteUsername(s.getRemitente().getUsername())
                .destinatarioId(s.getDestinatario().getId())
                .estado(s.getEstado())
                .fechaEnvio(s.getFechaEnvio())
                .fechaRespuesta(s.getFechaRespuesta())
                .mensaje(s.getMensaje())
                .build();
    }
}