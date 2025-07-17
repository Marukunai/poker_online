package com.pokeronline.logros.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogroDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private String iconoLogro;
}