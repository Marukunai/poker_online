package com.pokeronline.moderacion.dto;

import com.pokeronline.moderacion.model.MotivoSancion;
import com.pokeronline.moderacion.model.TipoSancion;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearSancionDTO {
    private Long userId;
    private TipoSancion tipo;
    private MotivoSancion motivo;
    private String descripcion;
    private Date fechaFin;
}
