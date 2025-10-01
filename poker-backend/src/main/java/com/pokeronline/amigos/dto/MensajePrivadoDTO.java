package com.pokeronline.amigos.dto;

import com.pokeronline.amigos.model.MensajePrivado;
import com.pokeronline.amigos.model.TipoMensaje;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensajePrivadoDTO {
    private Long id;
    private Long remitenteId;
    private Long destinatarioId;
    private TipoMensaje tipo;
    private String contenido;
    private LocalDateTime fechaEnvio;
    private Boolean leido;
    private LocalDateTime fechaLectura;
    private Integer duracionAudio;
    private Long mensajeRespondidoId;

    public static MensajePrivadoDTO fromEntity(MensajePrivado m) {
        return MensajePrivadoDTO.builder()
                .id(m.getId())
                .remitenteId(m.getRemitente().getId())
                .destinatarioId(m.getDestinatario().getId())
                .tipo(m.getTipo())
                .contenido(m.getContenido())
                .fechaEnvio(m.getFechaEnvio())
                .leido(m.getLeido())
                .fechaLectura(m.getFechaLectura())
                .duracionAudio(m.getDuracionAudio())
                .mensajeRespondidoId(m.getMensajeRespondido() != null ? m.getMensajeRespondido().getId() : null)
                .build();
    }
}