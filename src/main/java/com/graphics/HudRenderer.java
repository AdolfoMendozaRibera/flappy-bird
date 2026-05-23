package com.graphics;

/**
  Se ncargado de dibujar el HUD (Heads-Up Display) del juego.
  Usa la  geométricas (los cuadrados) para mostrar el estado del juego,
 */
public class HudRenderer {

    // Tamaño de cada pixel del HUD
    private static final float BLOQUE = 0.06f;
    private static final float MARGEN = 0.05f;

    /**
     Dibuja el HUD para un solo jugador.
     */
    public void render(Renderer renderer, Bird p1) {
        dibujarIndicadorJugador(renderer, -0.9f, 0.85f, p1.isVivo(), 0.9f, 0.8f, 0.1f);
        // Texto más pequeño y pegado al indicador
        dibujarTextoRetro(renderer, "P1: " + p1.getPuntaje(), -0.8f, 0.87f, 0.02f, 0.9f, 0.8f, 0.1f);
    }

    /**
     * Dibuja el HUD completo para ambos jugadores.
     * @param renderer El Renderer compartido del juego
     * @param p1       Objeto del Jugador 1
     * @param p2       Objeto del Jugador 2
     */
    public void render(Renderer renderer, Bird p1, Bird p2) {
        // Player 1 izquierda
        dibujarIndicadorJugador(renderer, -0.9f, 0.85f, p1.isVivo(), 0.9f, 0.8f, 0.1f);
        dibujarTextoRetro(renderer, "P1: " + p1.getPuntaje(), -0.8f, 0.87f, 0.02f, 0.9f, 0.8f, 0.1f);

        // Player 2 derecha
        dibujarIndicadorJugador(renderer, 0.4f, 0.85f, p2.isVivo(), 0.1f, 0.8f, 0.9f);
        dibujarTextoRetro(renderer, "P2: " + p2.getPuntaje(), 0.5f, 0.87f, 0.02f, 0.1f, 0.8f, 0.9f);
    }

    /**
     Dibuja el HUD para tres jugadores.
     */
    public void render(Renderer renderer, Bird p1, Bird p2, Bird p3) {
        // P1: Arriba Izquierda
        dibujarIndicadorJugador(renderer, -0.9f, 0.85f, p1.isVivo(), 0.9f, 0.8f, 0.1f);
        dibujarTextoRetro(renderer, "P1: " + p1.getPuntaje(), -0.8f, 0.87f, 0.02f, 0.9f, 0.8f, 0.1f);

        // P2: Arriba Centro
        dibujarIndicadorJugador(renderer, -0.2f, 0.85f, p2.isVivo(), 0.1f, 0.8f, 0.9f);
        dibujarTextoRetro(renderer, "P2: " + p2.getPuntaje(), -0.1f, 0.87f, 0.02f, 0.1f, 0.8f, 0.9f);

        // P3: Arriba Derecha
        dibujarIndicadorJugador(renderer, 0.4f, 0.85f, p3.isVivo(), 0.3f, 0.7f, 0.3f);
        dibujarTextoRetro(renderer, "P3: " + p3.getPuntaje(), 0.5f, 0.87f, 0.02f, 0.3f, 0.7f, 0.3f);
    }

    /**
     * Dibuja un cuadrado de color si el jugador está vivo, o gris si está muerto.
     */
    private void dibujarIndicadorJugador(Renderer renderer, float x, float y,
                                          boolean vivo, float r, float g, float b) {
        // Fondo negro para que resalte
        renderer.dibujarRectangulo(x, y, BLOQUE + 0.02f, BLOQUE + 0.02f, 0.0f, 0.0f, 0.0f);

        if (vivo) {
            renderer.dibujarRectangulo(x, y, BLOQUE, BLOQUE, r, g, b);
        } else {
            renderer.dibujarRectangulo(x, y, BLOQUE, BLOQUE, 0.4f, 0.4f, 0.4f);
        }
    }


    // ------------
    // Esto es para dibujar pixeles, como un motor
    //---------

    /**
     * Calcula el ancho total del texto para dibujarlo exactamente en el centro de la pantalla.
     */
    public void dibujarTextoCentrado(Renderer renderer, String texto, float startY, float size, float r, float g, float b) {
        // Cada letra ocupa 3 columnas de size, más 1 columna de size como espacio = 4 * size
        // Al final restamos 1 espacio porque la última letra no tiene espacio extra.
        float totalWidth = (texto.length() * 4 * size) - size;
        float startX = -totalWidth / 2.0f;
        dibujarTextoRetro(renderer, texto, startX, startY, size, r, g, b);
    }

    /**
     * Dibuja una palabra letra por letra usando la matriz.
     */
    public void dibujarTextoRetro(Renderer renderer, String texto, float startX, float startY, float size, float r, float g, float b) {
        float x = startX;
        for (char c : texto.toUpperCase().toCharArray()) {
            if (c == ' ') {
                x += size * 4; // Espacio en blanco
                continue;
            }
            dibujarLetra(renderer, c, x, startY, size, r, g, b);
            x += size * 4; // Avanzamos a la siguiente letra (3 columnas + 1 espacio)
        }
    }

    /**
     * Dibuja una sola letra basandose en una matriz de 5x3.
     */
    private void dibujarLetra(Renderer renderer, char letra, float x, float y, float size, float r, float g, float b) {
        // Cada letra es una cuadricula de 5 filas y 3 columnas.
        // 1 = Píxel encendido, 0 = Píxel apagado.
        int[] patron;
        switch (letra) {
            case '0': patron = new int[]{1,1,1, 1,0,1, 1,0,1, 1,0,1, 1,1,1}; break;
            case '1': patron = new int[]{0,1,0, 1,1,0, 0,1,0, 0,1,0, 1,1,1}; break;
            case '2': patron = new int[]{1,1,1, 0,0,1, 1,1,1, 1,0,0, 1,1,1}; break;
            case '3': patron = new int[]{1,1,1, 0,0,1, 1,1,1, 0,0,1, 1,1,1}; break;
            case '4': patron = new int[]{1,0,1, 1,0,1, 1,1,1, 0,0,1, 0,0,1}; break;
            case '5': patron = new int[]{1,1,1, 1,0,0, 1,1,1, 0,0,1, 1,1,1}; break;
            case '6': patron = new int[]{1,1,1, 1,0,0, 1,1,1, 1,0,1, 1,1,1}; break;
            case '7': patron = new int[]{1,1,1, 0,0,1, 0,1,0, 0,1,0, 0,1,0}; break;
            case '8': patron = new int[]{1,1,1, 1,0,1, 1,1,1, 1,0,1, 1,1,1}; break;
            case '9': patron = new int[]{1,1,1, 1,0,1, 1,1,1, 0,0,1, 1,1,1}; break;
            case ':': patron = new int[]{0,0,0, 0,1,0, 0,0,0, 0,1,0, 0,0,0}; break;
            case 'A': patron = new int[]{0,1,0, 1,0,1, 1,1,1, 1,0,1, 1,0,1}; break;
            case 'B': patron = new int[]{1,1,0, 1,0,1, 1,1,0, 1,0,1, 1,1,0}; break;
            case 'C': patron = new int[]{0,1,1, 1,0,0, 1,0,0, 1,0,0, 0,1,1}; break;
            case 'D': patron = new int[]{1,1,0, 1,0,1, 1,0,1, 1,0,1, 1,1,0}; break;
            case 'E': patron = new int[]{1,1,1, 1,0,0, 1,1,1, 1,0,0, 1,1,1}; break;
            case 'F': patron = new int[]{1,1,1, 1,0,0, 1,1,0, 1,0,0, 1,0,0}; break;
            case 'G': patron = new int[]{0,1,1, 1,0,0, 1,0,1, 1,0,1, 0,1,1}; break;
            case 'I': patron = new int[]{1,1,1, 0,1,0, 0,1,0, 0,1,0, 1,1,1}; break;
            case 'J': patron = new int[]{0,0,1, 0,0,1, 0,0,1, 1,0,1, 0,1,0}; break;
            case 'L': patron = new int[]{1,0,0, 1,0,0, 1,0,0, 1,0,0, 1,1,1}; break;
            case 'M': patron = new int[]{1,0,1, 1,1,1, 1,0,1, 1,0,1, 1,0,1}; break;
            case 'N': patron = new int[]{1,1,1, 1,0,1, 1,0,1, 1,0,1, 1,0,1}; break;
            case 'O': patron = new int[]{0,1,0, 1,0,1, 1,0,1, 1,0,1, 0,1,0}; break;
            case 'P': patron = new int[]{1,1,0, 1,0,1, 1,1,0, 1,0,0, 1,0,0}; break;
            case 'R': patron = new int[]{1,1,0, 1,0,1, 1,1,0, 1,0,1, 1,0,1}; break;
            case 'S': patron = new int[]{0,1,1, 1,0,0, 0,1,0, 0,0,1, 1,1,0}; break;
            case 'T': patron = new int[]{1,1,1, 0,1,0, 0,1,0, 0,1,0, 0,1,0}; break;
            case 'U': patron = new int[]{1,0,1, 1,0,1, 1,0,1, 1,0,1, 0,1,0}; break;
            case 'V': patron = new int[]{1,0,1, 1,0,1, 1,0,1, 1,0,1, 0,1,0}; break;
            case 'Y': patron = new int[]{1,0,1, 1,0,1, 0,1,0, 0,1,0, 0,1,0}; break;
            default:  patron = new int[]{1,1,1, 1,1,1, 1,1,1, 1,1,1, 1,1,1}; break; // Cuadrado si la letra no existe
        }

        // Bucle para pintar la matriz
        for (int fila = 0; fila < 5; fila++) {
            for (int col = 0; col < 3; col++) {
                if (patron[fila * 3 + col] == 1) {
                    float posX = x + (col * size);
                    float posY = y - (fila * size); // y va bajando
                    
                    // Borde negro ( con sombra)
                    renderer.dibujarRectangulo(posX + 0.005f, posY - 0.005f, size, size, 0.0f, 0.0f, 0.0f);
                    // Pixel a color
                    renderer.dibujarRectangulo(posX, posY, size, size, r, g, b);
                }
            }
        }
    }
}
