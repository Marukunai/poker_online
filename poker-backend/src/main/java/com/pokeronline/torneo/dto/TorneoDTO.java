package com.pokeronline.torneo.dto;

import com.pokeronline.torneo.model.Torneo;
import com.pokeronline.torneo.model.TorneoEstado;
import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TorneoDTO {
    private Long id;
    private String nombre;

    private Date fechaInicio;
    private Date fechaFin;

    // Métricas y configuración
    private int buyIn;
    private int fichasIniciales;
    private int premioTotal;

    // Puede ser null en tu modelo → lo dejamos como Integer
    private Integer minParticipantes;
    private int maxParticipantes;

    private boolean eliminacionDirecta;
    private TorneoEstado estado;

    private int nivelCiegasActual;
    private Date timestampInicioNivel;

    // Derivados (cómputos rápidos)
    private int numMesas;
    private int participantesActivos;

    public static TorneoDTO toDTO(Torneo t) {
        int numMesas = (t.getMesas() == null) ? 0 : t.getMesas().size();
        int activos = (t.getParticipantes() == null) ? 0 :
                (int) t.getParticipantes().stream().filter(p -> !p.isEliminado()).count();

        return TorneoDTO.builder()
                .id(t.getId())
                .nombre(t.getNombre())
                .fechaInicio(t.getFechaInicio())
                .fechaFin(t.getFechaFin())
                .buyIn(t.getBuyIn())
                .fichasIniciales(t.getFichasIniciales())
                .premioTotal(t.getPremioTotal())
                .minParticipantes(t.getMinParticipantes()) // puede ser null
                .maxParticipantes(t.getMaxParticipantes())
                .eliminacionDirecta(t.isEliminacionDirecta())
                .estado(t.getEstado())
                .nivelCiegasActual(t.getNivelCiegasActual())
                .timestampInicioNivel(t.getTimestampInicioNivel())
                .numMesas(numMesas)
                .participantesActivos(activos)
                .build();
    }
}