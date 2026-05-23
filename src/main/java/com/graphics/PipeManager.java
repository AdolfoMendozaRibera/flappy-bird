package com.graphics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Gestiona el conjunto de tuberías (obstáculos) del juego.
 * Se encarga de crearlas, moverlas y detectar colisiones.
 */
public class PipeManager {

    private class Pipe {
        private float x;
        private float gapY;
        private float gapSize; // Cada tubo recuerda su propio tamaño de hueco
        private boolean passedP1 = false;
        private boolean passedP2 = false;
        private boolean passedP3 = false;

        public Pipe(float x, float gapY, float gapSize) {
            this.x = x;
            this.gapY = gapY;
            this.gapSize = gapSize;
        }
    }

    private List<Pipe> pipes;
    private Random random;
    private float spawnTimer = 0.0f;

    // Configuración de las tuberías
    private static final float SPAWN_RATE = 1.5f; // Segundos entre tuberías (base)
    private static final float PIPE_SPEED = 0.6f; // Velocidad base
    private static final float PIPE_WIDTH = 0.15f;
    private static final float GAP_SIZE_BASE = 0.45f; // Hueco base
    private static final float GAP_SIZE_MAX = 0.50f; // Hueco maximo (a velocidad extrema)
    // Maximo desplazamiento vertical permitido entre un hueco y el siguiente.
    // Para garantizar que el recorrido siempre sea físicamente posible.
    private static final float MAX_GAP_DELTA = 0.50f;

    // Colores de la tuberia
    private static final float PR = 0.18f, PG = 0.65f, PB = 0.18f; // Color BASE
    private static final float PLR = 0.35f, PLG = 0.85f, PLB = 0.35f; // Color CLARO (Brillo)
    private static final float PDR = 0.08f, PDG = 0.40f, PDB = 0.08f; // Color OSCURO (Sombra)

    // Posición del ultimo hueco generado 
    private float lastGapY = 0.0f;

    public PipeManager() {
        pipes = new ArrayList<>();
        random = new Random();
    }

    public void update(float dt, boolean gameOver, int puntajeActual) {
        if (gameOver)
            return;

        // Dificultad progresiva: velocidad y spawn rate escalan con el puntaje
        // Ahora escala más lento: llega al máximo (~1.2f) cerca de los 40 puntos
        float currentSpeed = Math.min(PIPE_SPEED + (puntajeActual * 0.015f), 1.2f);
        float currentSpawnRate = Math.max(SPAWN_RATE - (puntajeActual * 0.02f), 0.8f);
        // A mayor velocidad, el hueco crece para compensar: el reto es precisión, no
        // imposibilidad.
        float speedRatio = (currentSpeed - PIPE_SPEED) / (1.2f - PIPE_SPEED); // 0.0 a 1.0
        float currentGap = GAP_SIZE_BASE + speedRatio * (GAP_SIZE_MAX - GAP_SIZE_BASE);

        // 1. Generar nueva tubería con aleatoriedad encadenada (Chained Randomness)
        spawnTimer += dt;
        if (spawnTimer >= currentSpawnRate) {
            spawnTimer = 0.0f;

            // El nuevo hueco es relativo al anterior: no puede saltar más de MAX_GAP_DELTA.
            // Esto garantiza que siempre haya un "camino" físicamente posible.
            float delta = (random.nextFloat() * 2.0f - 1.0f) * MAX_GAP_DELTA;
            float newGapY = lastGapY + delta;

            // Clampeamos para que los tubos nunca queden fuera de los límites de la
            // pantalla
            float gapHalf = currentGap / 2.0f;
            newGapY = Math.max(-0.5f + gapHalf, Math.min(0.5f - gapHalf, newGapY));

            lastGapY = newGapY; // Guardamos la posición para el siguiente tubo
            pipes.add(new Pipe(1.2f, newGapY, currentGap));
        }

        // 2. Mover y limpiar tuberías
        Iterator<Pipe> it = pipes.iterator();
        while (it.hasNext()) {
            Pipe p = it.next();
            p.x -= currentSpeed * dt;

            // Si se sale de la pantalla por la izquierda, la borramos
            if (p.x < -1.2f) {
                it.remove();
            }
        }
    }

    public void render(Renderer renderer) {
        for (Pipe p : pipes) {
            float gap = p.gapSize;

            // ========== TUBERIA DE ARRIBA ==========
            float topHeight = 1.0f - (p.gapY + gap / 2.0f);
            float topY = 1.0f - topHeight / 2.0f;

            renderer.dibujarRectangulo(p.x, topY, PIPE_WIDTH, topHeight, PR, PG, PB);
            renderer.dibujarRectangulo(p.x - PIPE_WIDTH * 0.28f, topY, PIPE_WIDTH * 0.18f, topHeight, PLR, PLG, PLB);
            renderer.dibujarRectangulo(p.x + PIPE_WIDTH * 0.35f, topY, PIPE_WIDTH * 0.12f, topHeight, PDR, PDG, PDB);

            float capY = p.gapY + gap / 2.0f + 0.04f;
            renderer.dibujarRectangulo(p.x, capY, PIPE_WIDTH * 1.35f, 0.08f, PR, PG, PB);
            renderer.dibujarRectangulo(p.x - PIPE_WIDTH * 0.40f, capY, PIPE_WIDTH * 0.18f, 0.08f, PLR, PLG, PLB);
            renderer.dibujarRectangulo(p.x + PIPE_WIDTH * 0.45f, capY, PIPE_WIDTH * 0.12f, 0.08f, PDR, PDG, PDB);

            // ========== TUBERIA DE ABAJO ==========
            float bottomHeight = (p.gapY - gap / 2.0f) - (-0.8f);
            float bottomY = -0.8f + bottomHeight / 2.0f;

            renderer.dibujarRectangulo(p.x, bottomY, PIPE_WIDTH, bottomHeight, PR, PG, PB);
            renderer.dibujarRectangulo(p.x - PIPE_WIDTH * 0.28f, bottomY, PIPE_WIDTH * 0.18f, bottomHeight, PLR, PLG,
                    PLB);
            renderer.dibujarRectangulo(p.x + PIPE_WIDTH * 0.35f, bottomY, PIPE_WIDTH * 0.12f, bottomHeight, PDR, PDG,
                    PDB);

            float bottomCapY = p.gapY - gap / 2.0f - 0.04f;
            renderer.dibujarRectangulo(p.x, bottomCapY, PIPE_WIDTH * 1.35f, 0.08f, PR, PG, PB);
            renderer.dibujarRectangulo(p.x - PIPE_WIDTH * 0.40f, bottomCapY, PIPE_WIDTH * 0.18f, 0.08f, PLR, PLG, PLB);
            renderer.dibujarRectangulo(p.x + PIPE_WIDTH * 0.45f, bottomCapY, PIPE_WIDTH * 0.12f, 0.08f, PDR, PDG, PDB);
        }
    }

    /**
     * Comprueba si el pájaro ha chocado con alguna tubería.
     */
    public boolean checkCollision(Bird bird) {
        float bX = bird.getX();
        float bY = bird.getY();
        float bW = bird.getAncho() * 0.8f;
        float bH = bird.getAlto() * 0.8f;

        for (Pipe p : pipes) {
            if (bX + bW / 2 > p.x - PIPE_WIDTH / 2 && bX - bW / 2 < p.x + PIPE_WIDTH / 2) {
                // Usamos el gapSize individual de cada tubería
                if (bY + bH / 2 > p.gapY + p.gapSize / 2 || bY - bH / 2 < p.gapY - p.gapSize / 2) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Verifica si algún pájaro ha cruzado una tubería y le suma un punto.
     */
    public void updateScore(Bird p1, Bird p2, Bird p3) {

        for (Pipe p : pipes) {
            boolean playedSoundThisFrame = false;

            // Si el Player 1 pasó la tubería y está vivo
            if (!p.passedP1 && p1.isVivo() && p.x + PIPE_WIDTH < p1.getX()) {
                p.passedP1 = true;
                p1.addPunto();
                playedSoundThisFrame = true;
            }
            // Si el Player 2 pasó la tubería y está vivo
            if (!p.passedP2 && p2.isVivo() && p.x + PIPE_WIDTH < p2.getX()) {
                p.passedP2 = true;
                p2.addPunto();
                playedSoundThisFrame = true;
            }
            // Si el Player 3 pasó la tubería y está vivo
            if (!p.passedP3 && p3.isVivo() && p.x + PIPE_WIDTH < p3.getX()) {
                p.passedP3 = true;
                p3.addPunto();
                playedSoundThisFrame = true;
            }

            // Reproducimos el sonido solo una vez por tubería, incluso si ambos pasan a la
            // vez
            if (playedSoundThisFrame) {
                SoundManager.playPointSound();
            }
        }
    }

    public void reset() {
        pipes.clear();
        spawnTimer = 0.0f;
        lastGapY = 0.0f; // Reiniciamos la memoria del generador al centro
    }
}
