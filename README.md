# Flappy Bird Parcial (OpenGL & Java)

Un motor gráfico 2D minimalista y clon de Flappy Bird construido desde cero utilizando **Java** y **OpenGL 3.3 (Core Profile)** a través de la librería **LWJGL 3**. 


## 🎮 Características Principales

*   **Multijugador Local (1 a 3 Jugadores):** Selección de modo de juego en la pantalla de inicio.
    *   **Jugador 1:** `ESPACIO` (Pájaro Amarillo)
    *   **Jugador 2:** `W` o `FLECHA ARRIBA` (Pájaro Celeste)
    *   **Jugador 3:** `K` (Pájaro Verde)
*   **Motor de Texto Retro (Procedural):** Renderizado de texto en tiempo real utilizando matrices binarias (5x3) sin depender de fuentes externas o texturas de sprites.
*   **Pipeline Gráfico Moderno:** Uso de **VAOs, VBOs y Shaders (GLSL)** personalizados para renderizar primitivas (Cuadrados, Triángulos, y Círculos usando *Triangle Fan*).
*   **Física y Colisiones:** Sistema de gravedad basado en Delta Time (`dt`) y colisiones precisas (AABB - Axis-Aligned Bounding Box).
*   **Dificultad Progresiva:** La velocidad del juego y la aparición de las tuberías se adaptan al puntaje del jugador.
*   **Mecánica de Gravedad Inversa:** ¡Al alcanzar los 5 puntos, el pajaro sube hacia arriba, terminando en game over (puedes cambiardo en el archivo Bird.java, en la linea 80)
*   **Efecto Parallax:** Fondo multicapa (Nubes, Ciudad, Arbustos) con velocidad de desplazamiento asíncrona.

## 🛠️ Tecnologías y Arquitectura

*   **Lenguaje:** Java
*   **Librerías:** LWJGL 3 (Lightweight Java Game Library) - OpenGL, GLFW.
*   **Gestor de Dependencias:** Maven
*   **Arquitectura:** 
    *   `Renderer.java`: Capa de abstracción de bajo nivel. El "Escultor y Pintor". Gestiona la memoria de la GPU y los Shaders.
    *   `Main.java`: Máquina de estados (INICIO, JUGANDO, GAME_OVER) y Game Loop.
    *   `Bird.java` & `PipeManager.java`: Entidades del juego con lógica matemática (como la rotación orbital usando matrices 2D).

## 🚀 Cómo Ejecutar

Asegúrate de tener **Java (JDK)** y **Maven** instalados en tu sistema.

1.  Clona el repositorio.
2.  Navega hasta la carpeta raíz del proyecto (`flappy-bird-parcial`).
3.  Ejecuta el siguiente comando en tu terminal:

```bash
mvn compile exec:exec
```
