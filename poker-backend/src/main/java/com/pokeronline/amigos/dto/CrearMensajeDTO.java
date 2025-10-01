package com.pokeronline.amigos.dto;

import com.pokeronline.amigos.model.TipoMensaje;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearMensajeDTO {
    private Long destinatarioId;
    private TipoMensaje tipo;       // TEXTO, AUDIO, GIF, STICKER, IMAGEN...
    private String contenido;       // texto o URL
    private Integer duracionAudio;  // opcional
    private Long mensajeRespondidoId;
}