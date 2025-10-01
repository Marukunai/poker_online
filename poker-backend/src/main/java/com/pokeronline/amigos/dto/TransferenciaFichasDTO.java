package com.pokeronline.amigos.dto;

import com.pokeronline.amigos.model.EstadoTransferencia;
import com.pokeronline.amigos.model.TransferenciaFichas;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaFichasDTO {
    private Long id;
    private Long remitenteId;
    private Long destinatarioId;
    private Long cantidad;
    private String mensaje;
    private Boolean esRegalo;
    private EstadoTransferencia estado;
    private LocalDateTime fecha;

    public static TransferenciaFichasDTO fromEntity(TransferenciaFichas t) {
        return TransferenciaFichasDTO.builder()
                .id(t.getId())
                .remitenteId(t.getRemitente().getId())
                .destinatarioId(t.getDestinatario().getId())
                .cantidad(t.getCantidad())
                .mensaje(t.getMensaje())
                .esRegalo(Boolean.TRUE.equals(t.getEsRegalo()))
                .estado(t.getEstado())
                .fecha(t.getFecha())
                .build();
    }
}
