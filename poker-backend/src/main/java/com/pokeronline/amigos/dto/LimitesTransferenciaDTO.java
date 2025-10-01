package com.pokeronline.amigos.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimitesTransferenciaDTO {
    private long limiteDiario;
    private long usadoHoy;
    private long restanteHoy;
    private long limitePorTransferencia;
    private long transferenciaMinima;
    private int transferenciasRealizadasHoy;
    private int transferenciasRestantesHoy;
    private double comisionPorcentaje;
}
