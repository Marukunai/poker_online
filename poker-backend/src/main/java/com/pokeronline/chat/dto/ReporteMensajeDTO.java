package com.pokeronline.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteMensajeDTO {
    private Long mensajeId;
    private Long reportadoPorId;
    private String razon; // opcional
}