package com.graphics;

import org.lwjgl.glfw.GLFW;

/**
 * Encargado de gestionar los eventos del teclado.
 * Regla de Separación: Main no debe saber CÓMO se leen las teclas, solo pregunta SI se presionó una.
 */
public class InputManager {

    // Arreglo para recordar el estado de todas las teclas
    private boolean[] teclas = new boolean[GLFW.GLFW_KEY_LAST];

    public void init(long window) {
        // Le decimos a GLFW: "Cuando el usuario presione una tecla, ejecuta esta función"
        GLFW.glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            
            // Si presionamos ESC, le decimos a la ventana que debe cerrarse
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
                GLFW.glfwSetWindowShouldClose(window, true);
            }

            // Guardamos si la tecla está presionada o fue soltada
            if (key >= 0 && key < GLFW.GLFW_KEY_LAST) {
                if (action == GLFW.GLFW_PRESS) {
                    teclas[key] = true;
                } else if (action == GLFW.GLFW_RELEASE) {
                    teclas[key] = false;
                }
            }
        });
    }

    /**
     * Devuelve true si la tecla solicitada está actualmente presionada.
     */
    public boolean isKeyPressed(int key) {
        return teclas[key];
    }
}
