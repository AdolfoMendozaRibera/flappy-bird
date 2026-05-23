package com.graphics;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

/**
 * Clase Principal Maneja el ciclo de vida del juego (Loop) y la ventana.
 * Solo coordina pero no dibuja
 */
public class Main {

    private long window;
    private static final int ANCHO = 900;
    private static final int ALTO = 700;

    // Nuestras clases de soporte
    private Renderer renderer;
    private HudRenderer hud;
    private InputManager input;

    // Jugadores y obstáculos
    private Bird player1;
    private Bird player2;
    private Bird player3;
    private PipeManager tuberias;
    private boolean prevSpace = false; // Control P1
    private boolean prevW = false; // Control P2
    private boolean prevK = false; // Control P3

    // Maquina de Estados: el juego siempre vive en uno de estos tres estados
    private GameState estado = GameState.INICIO;

    // Para la animación del suelo
    private float tiempoGlobal = 0.0f;

    // Modo de juego (1, 2 o 3 jugadores)
    private int numPlayers = 1;

    // Parallax
    private float nube1X = -0.5f, nube2X = 0.3f, nube3X = 1.1f;
    private float ciudad1X = -0.8f, ciudad2X = 0.0f, ciudad3X = 0.8f, ciudad4X = 1.6f;
    private float arbusto1X = -0.6f, arbusto2X = 0.0f, arbusto3X = 0.6f, arbusto4X = 1.2f, arbusto5X = 1.8f;

    // -------------------------------------------------------------------------

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        // 1. Iniciar GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("No se pudo iniciar GLFW");
        }

        // 2. Configurar ventana 
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);

        // VENTANA FIJA, No permite que el usuario la redimensione
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);

        window = GLFW.glfwCreateWindow(ANCHO, ALTO, "Flappy Bird Parcial - 2P | ESPACIO para jugar", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Error al crear la ventana");
        }

        // 3. Hacer el contexto actual e iniciar OpenGL
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1); // VSync
        GL.createCapabilities();

        // 4. Inicializar clases de soporte
        input = new InputManager();
        input.init(window);

        renderer = new Renderer();
        renderer.init();

        hud = new HudRenderer();

        // 5. Crear jugadores y tuberias
        crearJugadores();
        tuberias = new PipeManager();

        System.out.println("OpenGL inicializado correctamente.");
    }

    /**
     * Crea (o recrea) los dos o tres pájaros en sus posiciones iniciales.
     * Centralizado aquí para no repetir código al reiniciar.
     */
    private void crearJugadores() {
        player1 = new Bird(-0.5f, 0.1f, 0.9f, 0.8f, 0.1f); // Amarillo
        player2 = new Bird(-0.5f, -0.1f, 0.1f, 0.8f, 0.9f); // Celeste 
        player3 = new Bird(-0.5f, 0.0f, 0.3f, 0.7f, 0.3f); // verde
    }

    // -------------------------------------------------------------------------

    private void loop() {
        float ultimoTiempo = (float) GLFW.glfwGetTime();

        while (!GLFW.glfwWindowShouldClose(window)) {

            // Calcular Delta Time
            float ahora = (float) GLFW.glfwGetTime();
            float dt = ahora - ultimoTiempo;
            ultimoTiempo = ahora;

            // Avanzamos el tiempo global solo si no estamos en Game Over (para animar el
            // suelo)
            if (estado != GameState.GAME_OVER) {
                tiempoGlobal += dt;
            }

            // 1. Procesar Input
            GLFW.glfwPollEvents();
            if (GLFW.glfwWindowShouldClose(window))
                break;

            // 2. Actualizar lógica según el estado actual
            switch (estado) {
                case INICIO -> updateInicio();
                case JUGANDO -> updateJugando(dt);
                case GAME_OVER -> updateGameOver();
            }

            // 3. Dibujar según el estado actual
            switch (estado) {
                case INICIO -> renderInicio();
                case JUGANDO -> renderJugando();
                case GAME_OVER -> renderGameOver();
            }

            // 4. Mostrar frame
            GLFW.glfwSwapBuffers(window);
        }
    }

    // -----
    // ESTADO: INICIO
    // -----

    private void updateInicio() {
        // Transición: 1, 2 o 3 determinan el modo de juego
        boolean key1 = input.isKeyPressed(GLFW.GLFW_KEY_1);
        boolean key2 = input.isKeyPressed(GLFW.GLFW_KEY_2);
        boolean key3 = input.isKeyPressed(GLFW.GLFW_KEY_3);

        if (key1 || key2 || key3) {
            estado = GameState.JUGANDO;
            if (key1) {
                numPlayers = 1;
                player2.setMuerto();
                player3.setMuerto();
                GLFW.glfwSetWindowTitle(window, "Flappy Bird Parcial - 1P | Puntos: 0");
            } else if (key2) {
                numPlayers = 2;
                player3.setMuerto();
                GLFW.glfwSetWindowTitle(window, "Flappy Bird Parcial - 2P | Puntos: 0");
            } else {
                numPlayers = 3;
                GLFW.glfwSetWindowTitle(window, "Flappy Bird Parcial - 3P | Puntos: 0");
            }
        }
    }

    private void renderInicio() {
        // Fondo con degradado (Cielo Verde Oscuro)
        renderer.dibujarFondoDegradado(0.1f, 0.4f, 0.1f, 0.05f, 0.2f, 0.05f);

        // Suelo estatico verde
        renderer.dibujarRectangulo(0.0f, -0.9f, 2.0f, 0.2f, 0.2f, 0.8f, 0.2f);

        // Titulo principal 
        hud.dibujarTextoCentrado(renderer, "FLAPPY BIRD", 0.5f, 0.04f, 1.0f, 0.8f, 0.1f);

        // Opciones de seleccion
        hud.dibujarTextoCentrado(renderer, "1 PARA 1 JUGADOR", 0.15f, 0.025f, 1.0f, 1.0f, 1.0f);
        hud.dibujarTextoCentrado(renderer, "2 PARA 2 JUGADORES", 0.0f, 0.025f, 1.0f, 1.0f, 1.0f);
        hud.dibujarTextoCentrado(renderer, "3 PARA 3 JUGADORES", -0.15f, 0.025f, 1.0f, 1.0f, 1.0f);
    }

    // =========================================================================
    // ESTADO: JUGANDO
    // =========================================================================

    private void updateJugando(float dt) {
        // Logica Player 1 (ESPACIO)
        if (player1.isVivo()) {
            boolean space = input.isKeyPressed(GLFW.GLFW_KEY_SPACE);
            if (space && !prevSpace)
                player1.jump();
            prevSpace = space;

            player1.update(dt);
            if (player1.getY() < -0.8f || player1.getY() > 1.0f || tuberias.checkCollision(player1)) {
                SoundManager.playCrashSound();
                player1.setMuerto();
            }
        }

        // Lógica Player 2 (W o Flecha Arriba)
        if (player2.isVivo()) {
            boolean w = input.isKeyPressed(GLFW.GLFW_KEY_W) || input.isKeyPressed(GLFW.GLFW_KEY_UP);
            if (w && !prevW)
                player2.jump();
            prevW = w;

            player2.update(dt);
            if (player2.getY() < -0.8f || player2.getY() > 1.0f || tuberias.checkCollision(player2)) {
                SoundManager.playCrashSound();
                player2.setMuerto();
            }
        }

        // Lógica Player 3 (Tecla K)
        if (player3.isVivo()) {
            boolean k = input.isKeyPressed(GLFW.GLFW_KEY_K);
            if (k && !prevK)
                player3.jump();
            prevK = k;

            player3.update(dt);
            if (player3.getY() < -0.8f || player3.getY() > 1.0f || tuberias.checkCollision(player3)) {
                SoundManager.playCrashSound();
                player3.setMuerto();
            }
        }

        // Actualizar tuberías con dificultad progresiva basada en el mejor puntaje
        int mejorPuntaje = Math.max(player1.getPuntaje(), Math.max(player2.getPuntaje(), player3.getPuntaje()));
        tuberias.update(dt, false, mejorPuntaje);

        // Mover fondos con efecto Parallax
        nube1X -= 0.05f * dt;
        nube2X -= 0.05f * dt;
        nube3X -= 0.05f * dt;
        if (nube1X < -1.3f)
            nube1X += 2.4f;
        if (nube2X < -1.3f)
            nube2X += 2.4f;
        if (nube3X < -1.3f)
            nube3X += 2.4f;

        ciudad1X -= 0.15f * dt;
        ciudad2X -= 0.15f * dt;
        ciudad3X -= 0.15f * dt;
        ciudad4X -= 0.15f * dt;
        if (ciudad1X < -1.6f)
            ciudad1X += 3.2f;
        if (ciudad2X < -1.6f)
            ciudad2X += 3.2f;
        if (ciudad3X < -1.6f)
            ciudad3X += 3.2f;
        if (ciudad4X < -1.6f)
            ciudad4X += 3.2f;

        arbusto1X -= 0.40f * dt;
        arbusto2X -= 0.40f * dt;
        arbusto3X -= 0.40f * dt;
        arbusto4X -= 0.40f * dt;
        arbusto5X -= 0.40f * dt;
        if (arbusto1X < -1.2f)
            arbusto1X += 3.0f;
        if (arbusto2X < -1.2f)
            arbusto2X += 3.0f;
        if (arbusto3X < -1.2f)
            arbusto3X += 3.0f;
        if (arbusto4X < -1.2f)
            arbusto4X += 3.0f;
        if (arbusto5X < -1.2f)
            arbusto5X += 3.0f;

        // Actualizar puntajes individuales
        tuberias.updateScore(player1, player2, player3);

        if (numPlayers == 3) {
            GLFW.glfwSetWindowTitle(window, "P1: " + player1.getPuntaje() + " | P2: " + player2.getPuntaje() + " | P3: " + player3.getPuntaje());
        } else if (numPlayers == 2) {
            GLFW.glfwSetWindowTitle(window, "P1: " + player1.getPuntaje() + " | P2: " + player2.getPuntaje());
        } else {
            GLFW.glfwSetWindowTitle(window, "P1: " + player1.getPuntaje());
        }

        // Transicion: Game Over
        boolean anyAlive = player1.isVivo() || player2.isVivo() || player3.isVivo();
        if (!anyAlive) {
            estado = GameState.GAME_OVER;
            GLFW.glfwSetWindowTitle(window, "Flappy Bird - GAME OVER | R para reiniciar");
        }
    }

    private void renderJugando() {
        // Cielo azul con degradado
        renderer.dibujarFondoDegradado(0.2f, 0.5f, 0.9f, 0.6f, 0.8f, 1.0f);

        // --- NUBES (parallax más lento, esponjosas) ---
        dibujarNube(nube1X, 0.55f, 0.28f);
        dibujarNube(nube2X, 0.65f, 0.20f);
        dibujarNube(nube3X, 0.48f, 0.25f);

        // --- CIUDAD (Fondo lejano, anclada al suelo) ---
        dibujarCiudadLineal(ciudad1X);
        dibujarCiudadLineal(ciudad2X);
        dibujarCiudadLineal(ciudad3X);
        dibujarCiudadLineal(ciudad4X);

        // --- ARBUSTOS (Fondo medio, anclados al suelo) ---
        dibujarArbusto(arbusto1X, 0.25f);
        dibujarArbusto(arbusto2X, 0.35f);
        dibujarArbusto(arbusto3X, 0.20f);
        dibujarArbusto(arbusto4X, 0.30f);
        dibujarArbusto(arbusto5X, 0.25f);

        // --- TUBERIAS y PAJAROS ---
        tuberias.render(renderer);
        if (player1.isVivo()) player1.render(renderer);
        if (player2.isVivo()) player2.render(renderer);
        if (player3.isVivo()) player3.render(renderer);

        // HUD dinámico
        if (numPlayers == 3) {
            hud.render(renderer, player1, player2, player3);
        } else if (numPlayers == 2) {
            hud.render(renderer, player1, player2);
        } else {
            hud.render(renderer, player1);
        }

        // --- SUELO (encima de tuberías, debajo del HUD) ---
        dibujarSuelo();
    }

    // =========================================================================
    // ESTADO: GAME OVER
    // =========================================================================

    private void updateGameOver() {
        // Transición: R reinicia la partida completa
        if (input.isKeyPressed(GLFW.GLFW_KEY_R)) {
            crearJugadores();
            tuberias.reset();
            prevSpace = false;
            prevW = false;
            prevK = false;
            estado = GameState.INICIO;
            GLFW.glfwSetWindowTitle(window, "Flappy Bird Parcial - 2P | ESPACIO para jugar");
        }
    }

    private void renderGameOver() {
        // Cielo rojo con degradado
        renderer.dibujarFondoDegradado(0.8f, 0.2f, 0.2f, 0.4f, 0.1f, 0.1f);

        tuberias.render(renderer);

        // Cartel de "GAME OVER" centrado
        hud.dibujarTextoCentrado(renderer, "GAME OVER", 0.4f, 0.05f, 1.0f, 1.0f, 1.0f);

        // Mostrar puntajes finales
        if (numPlayers == 3) {
            hud.dibujarTextoCentrado(renderer, "P1:" + player1.getPuntaje() + " P2:" + player2.getPuntaje() + " P3:" + player3.getPuntaje(), 0.1f, 0.025f, 1.0f, 1.0f, 1.0f);
        } else if (numPlayers == 2) {
            hud.dibujarTextoCentrado(renderer, "P1:" + player1.getPuntaje() + "  P2:" + player2.getPuntaje(), 0.1f, 0.03f, 1.0f, 1.0f, 1.0f);
        } else {
            hud.dibujarTextoCentrado(renderer, "PUNTOS: " + player1.getPuntaje(), 0.1f, 0.03f, 1.0f, 1.0f, 1.0f);
        }
        
        // Instrucción para reiniciar
        hud.dibujarTextoCentrado(renderer, "PRESIONA R PARA REINICIAR", -0.2f, 0.02f, 1.0f, 1.0f, 0.0f);

        // Suelo también en game over
        dibujarSuelo();
    }

    // =========================================================================

    /**
     * Dibuja el suelo con textura de césped (base verde + franja oscura + puntos).
     */
    private void dibujarSuelo() {
        // Base del suelo (verde medio)
        renderer.dibujarRectangulo(0.0f, -0.9f, 2.0f, 0.2f, 0.22f, 0.72f, 0.22f);
        // Capa superior del suelo (verde césped más brillante)
        renderer.dibujarRectangulo(0.0f, -0.80f, 2.0f, 0.025f, 0.35f, 0.85f, 0.35f);
        // Línea de sombra debajo del césped
        renderer.dibujarRectangulo(0.0f, -0.825f, 2.0f, 0.015f, 0.10f, 0.45f, 0.10f);
        // Tierra (marrón oscuro en la mitad inferior)
        renderer.dibujarRectangulo(0.0f, -0.95f, 2.0f, 0.12f, 0.42f, 0.28f, 0.10f);
    }

    private void dibujarNube(float cx, float cy, float sz) {
        float r = 0.98f, g = 0.98f, b = 0.98f;
        // Nube esponjosa de círculos solapados
        renderer.dibujarCirculo(cx, cy, sz, r, g, b);
        renderer.dibujarCirculo(cx - sz * 0.35f, cy - sz * 0.1f, sz * 0.7f, r, g, b);
        renderer.dibujarCirculo(cx + sz * 0.35f, cy - sz * 0.1f, sz * 0.7f, r, g, b);
    }

    private void dibujarEdificio(float cx, float ancho, float alto, float r, float g, float b) {
        // base = -0.79f garantiza que el suelo tape la parte inferior y no haya
        // espacios flotantes
        float baseCy = -0.79f;
        float cy = baseCy + alto / 2.0f;
        renderer.dibujarRectangulo(cx, cy, ancho, alto, r, g, b);
        // Borde superior (techo ligeramente más claro)
        renderer.dibujarRectangulo(cx, cy + alto / 2.0f - 0.01f, ancho, 0.02f,
                Math.min(r * 1.2f, 1f), Math.min(g * 1.2f, 1f), Math.min(b * 1.2f, 1f));
    }

    private void dibujarCiudadLineal(float cx) {
        float r = 0.45f, g = 0.65f, b = 0.55f; // Verde-azulado pastel retro
        dibujarEdificio(cx - 0.3f, 0.18f, 0.35f, r, g, b);
        dibujarEdificio(cx - 0.1f, 0.15f, 0.55f, r * 0.9f, g * 0.9f, b * 0.9f);
        dibujarEdificio(cx + 0.1f, 0.20f, 0.40f, r * 0.85f, g * 0.85f, b * 0.85f);
        dibujarEdificio(cx + 0.3f, 0.16f, 0.65f, r, g, b);

        // Antenas finas
        renderer.dibujarRectangulo(cx - 0.1f, -0.79f + 0.55f + 0.05f, 0.01f, 0.1f, r, g, b);
        renderer.dibujarRectangulo(cx + 0.3f, -0.79f + 0.65f + 0.08f, 0.02f, 0.16f, r, g, b);
    }

    private void dibujarArbusto(float cx, float sz) {
        float baseCy = -0.79f; // Justo en la línea del césped
        float r = 0.3f, g = 0.8f, b = 0.2f; // Verde manzana

        // Círculos principales. Como el suelo se dibuja DESPUÉS, cortará estos círculos
        // por la mitad, creando la ilusión perfecta de arbustos plantados en la tierra.
        renderer.dibujarCirculo(cx, baseCy, sz, r, g, b);
        renderer.dibujarCirculo(cx - sz * 0.4f, baseCy - sz * 0.1f, sz * 0.75f, r * 0.9f, g * 0.9f, b * 0.9f);
        renderer.dibujarCirculo(cx + sz * 0.4f, baseCy - sz * 0.1f, sz * 0.75f, r * 0.8f, g * 0.8f, b * 0.8f);
    }

    private void cleanup() {
        SoundManager.shutdown();
        renderer.cleanup();
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
