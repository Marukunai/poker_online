package com.pokeronline.model;

public enum EstiloBot {
    DEFAULT,
    AGRESIVO,       // Sube con frecuencia, juega manos fuertes y débiles
    CONSERVADOR,    // Solo juega manos fuertes y pocas veces hace raise
    LOOSE,          // Juega muchas manos aunque sean malas
    TIGHT           // Solo juega manos muy fuertes, poco margen
}