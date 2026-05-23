package com.graphics;

/**
 * Esto sirve para los posibles estados del juego.
 * Solo puede existir un estado a la vez
 */
public enum GameState {
    INICIO,     // Pantalla de bienvenida esperando al jugador
    JUGANDO,    // El juego está activo
    GAME_OVER   // el jugador o los jugadores han muerto
}
