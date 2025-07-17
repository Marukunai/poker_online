package com.pokeronline.logros.dto;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogroUsuarioDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String iconoLogro;
    private boolean obtenido;
    private String fechaObtencion;
}