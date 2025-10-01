package com.pokeronline.amigos.model;

public enum EstadoConexion {
    ONLINE,          // Conectado, navegando
    EN_PARTIDA,      // Jugando activamente
    EN_TORNEO,       // Participando en torneo
    AUSENTE,         // AFK (sin actividad 10+ min)
    NO_MOLESTAR,     // Configurado manualmente
    INVISIBLE,       // Para salir como desconectado pero sin estarlo
    OFFLINE          // Desconectado
}