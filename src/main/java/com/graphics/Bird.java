package com.graphics;

/**
 * Clase que representa al (Pajaro).
 * Esto maneja su propia física y se dibuja usando el Renderer.
 */
public class Bird {

    // Posición y dimensiones base
    private float x;
    private float y;
    private final float ancho = 0.1f;
    private final float alto = 0.1f;

    // Color personalizado
    private float colorR, colorG, colorB;

    // Estado y Puntaje
    private boolean vivo = true;
    private int puntaje = 0;

    // Física
    private float velocidadY = 0.0f;
    private float GRAVEDAD = -1.9f;
    private static final float IMPULSO_SALTO = 0.85f;
    private static final float VELOCIDAD_MAX_CAIDA = -1.8f;

    // Animación
    private float tiempoAleteo = 0.0f;
    private float tiempoParpadeo = 0.0f; // Tiempo del parpadeo
    private boolean parpadeando = false; // Si es que el ojo está cerrado
    private float inclinacionBase = 0.0f;

    public Bird(float startX, float startY, float r, float g, float b) {
        this.x = startX;
        this.y = startY;
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
    }

    /**
     * Aplica la gravedad y actualiza la posición.
     * 
     * @param dt Delta Time para suavizar el movimiento.
     */
    public void update(float dt) {
        if (!vivo)
            return; // Si está muerto, no se mueve ni cae

        // 1. La gravedad tira del pajaro hacia abajo
        velocidadY += GRAVEDAD * dt;

        // 2. Limitamos la caida para que no atraviese el suelo como un balazo
        if (velocidadY < VELOCIDAD_MAX_CAIDA) {
            velocidadY = VELOCIDAD_MAX_CAIDA;
        }

        // 3. Movemos al pajaro
        y += velocidadY * dt;

        // 4. Calculamos la inclinación basada en la velocidad
        inclinacionBase = velocidadY * 20.0f;

        // 5. Temporizador del aleteo
        tiempoAleteo += dt * 15.0f;

        // 6. Temporizador del parpadeo
        tiempoParpadeo += dt;
        if (!parpadeando && tiempoParpadeo > 3.5f) {
            // Cada 3.5 segundos, cierra el ojo
            parpadeando = true;
            tiempoParpadeo = 0.0f;
        } else if (parpadeando && tiempoParpadeo > 0.12f) {
            // El parpadeo dura 0.12 segundos
            parpadeando = false;
        }

        // Si alcanza el puntaje esperado, sube hacia arriba
        if (puntaje >= 5) {
            this.GRAVEDAD = 2.2f; // Ahora cae hacia ARRIBA
        } else {
            this.GRAVEDAD = -1.9f; // Gravedad normal
        }
    }

    /**
     * Hace saltar al pájaro (Cancela la caída y lo impulsa arriba).
     */
    public void jump() {
        velocidadY = IMPULSO_SALTO;
        SoundManager.playJumpSound();
    }

    /**
     * Calcula la posición (X,Y) de una pieza del pájaro rotada alrededor del centro
     * del cuerpo.
     */
    private float[] posRot(float dx, float dy) {
        float rad = (float) Math.toRadians(inclinacionBase);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);
        // Matriz de rotación 2D estándar
        float rotX = dx * cos - dy * sin;
        float rotY = dx * sin + dy * cos;
        return new float[] { x + rotX, y + rotY };
    }

    /**
     * Dibuja el pájaro con sistema de 3 FRAMES reales:
     * El ala cambia de POSICIÓN (no solo ángulo) en cada frame,
     * simulando un sprite de 8-bits. El pico es fijo.
     */
    public void render(Renderer renderer) {
        if (!vivo)
            return;

        // Determinar frame según el seno: 0=ala arriba, 1=medio, 2=abajo
        float seno = (float) Math.sin(tiempoAleteo);
        int frame = (seno > 0.33f) ? 0 : (seno > -0.33f) ? 1 : 2;

        // --- COLA (Triángulo) ---
        float[] pCola = posRot(-0.065f, 0.0f);
        renderer.dibujarTriangulo(pCola[0], pCola[1], ancho * 0.5f, alto * 0.45f,
                colorR * 0.65f, colorG * 0.65f, colorB * 0.65f, inclinacionBase + 180.0f);

        // --- CUERPO: Círculo (Triangle Fan) con outline negro y panza más clara ---
        // Outline (Centro = 0,0)
        renderer.dibujarEllipse(x, y, ancho + 0.016f, alto + 0.016f,
                0.0f, 0.0f, 0.0f, inclinacionBase);
        // Color principal
        renderer.dibujarEllipse(x, y, ancho, alto,
                colorR, colorG, colorB, inclinacionBase);
        // Panza más clara (elipse pequeña desplazada)
        float[] pPanza = posRot(-0.008f, -0.008f);
        renderer.dibujarEllipse(pPanza[0], pPanza[1], ancho * 0.58f, alto * 0.52f,
                Math.min(colorR + 0.35f, 1.0f),
                Math.min(colorG + 0.35f, 1.0f),
                Math.min(colorB + 0.35f, 1.0f), inclinacionBase);

        // --- ALA: rectángulos animados (cambia FORMA y POSICIÓN por frame) ---
        float[] pAlaBase, pAlaPunta;
        switch (frame) {
            case 0: // Ala ARRIBA (sobre el cuerpo)
                pAlaBase = posRot(-0.005f, 0.048f);
                renderer.dibujarRectanguloRotado(pAlaBase[0], pAlaBase[1],
                        ancho * 0.60f, alto * 0.22f, 0.9f, 0.55f, 0.1f, inclinacionBase - 10f);
                pAlaPunta = posRot(-0.015f, 0.038f);
                renderer.dibujarRectanguloRotado(pAlaPunta[0], pAlaPunta[1],
                        ancho * 0.38f, alto * 0.14f, 0.7f, 0.38f, 0.05f, inclinacionBase - 18f);
                break;
            case 1: // Ala MEDIO (alargada horizontalmente)
                pAlaBase = posRot(-0.008f, 0.005f);
                renderer.dibujarRectanguloRotado(pAlaBase[0], pAlaBase[1],
                        ancho * 0.68f, alto * 0.22f, 0.9f, 0.55f, 0.1f, inclinacionBase);
                pAlaPunta = posRot(-0.012f, 0.003f);
                renderer.dibujarRectanguloRotado(pAlaPunta[0], pAlaPunta[1],
                        ancho * 0.42f, alto * 0.14f, 0.7f, 0.38f, 0.05f, inclinacionBase);
                break;
            case 2: // Ala ABAJO (bajo el cuerpo)
                pAlaBase = posRot(-0.005f, -0.048f);
                renderer.dibujarRectanguloRotado(pAlaBase[0], pAlaBase[1],
                        ancho * 0.60f, alto * 0.22f, 0.9f, 0.55f, 0.1f, inclinacionBase + 10f);
                pAlaPunta = posRot(-0.015f, -0.038f);
                renderer.dibujarRectanguloRotado(pAlaPunta[0], pAlaPunta[1],
                        ancho * 0.38f, alto * 0.14f, 0.7f, 0.38f, 0.05f, inclinacionBase + 18f);
                break;
        }

        // --- OJO circular con parpadeo ---
        float[] pOjo = posRot(0.025f, 0.022f);
        // Outline oscuro del ojo
        renderer.dibujarCirculo(pOjo[0], pOjo[1], ancho * 0.28f, 0.0f, 0.0f, 0.0f);
        if (parpadeando) {
            // Ojo cerrado: línea horizontal delgada (rectángulo muy bajo)
            renderer.dibujarRectanguloRotado(pOjo[0], pOjo[1], ancho * 0.22f, alto * 0.06f,
                    0.0f, 0.0f, 0.0f, inclinacionBase);
        } else {
            // Esclera blanca (círculo blanco)
            renderer.dibujarCirculo(pOjo[0], pOjo[1], ancho * 0.22f, 1.0f, 1.0f, 1.0f);
            // Pupila negra (círculo pequeño desplazado hacia adelante)
            float[] pPupila = posRot(0.032f, 0.019f);
            renderer.dibujarCirculo(pPupila[0], pPupila[1], ancho * 0.11f, 0.0f, 0.0f, 0.0f);
        }

        // --- PICO FIJO (Triángulo) ---
        float[] pPico = posRot(0.062f, 0.0f);
        renderer.dibujarTriangulo(pPico[0], pPico[1],
                ancho * 0.45f, alto * 0.30f, 0.95f, 0.45f, 0.05f, inclinacionBase);
    }

    // Getters y Setters
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isVivo() {
        return vivo;
    }

    public void setMuerto() {
        this.vivo = false;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public void addPunto() {
        this.puntaje++;
    }

    public float getAncho() {
        return ancho;
    }

    public float getAlto() {
        return alto;
    }
}
