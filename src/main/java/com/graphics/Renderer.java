package com.graphics;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Encargado de dibujar en pantalla usando OpenGL.
 * Solo esta clase sabe de VAOs, VBOs y Shaders.
 */
public class Renderer {

    private int programa;
    private int vaoQuad, vboQuad;
    private int vaoTri, vboTri;
    private int vaoCircle, vboCircle;
    private static final int CIRCLE_SEGMENTS = 32; // Segmentos del círculo (mientras mas numero, es mas redondo)

    // Identificadores de los Uniforms para comunicarnos con el Shader
    private int uOffsetLocation;
    private int uScaleLocation;
    private int uColorLocation;
    private int uRotZLocation; // Para la rotación
    private int uUseGradientLocation;
    private int uColorBottomLocation;

    public void init() {
        crearShaders();
        crearQuadBase();
        crearTrianguloBase();
        crearCirculoBase();
    }

    private void crearShaders() {
        // Es el Vertex Shader: Calcula la posicion final (Molde * Escala + Posicion)
        String vertexSrc = """
                #version 330 core
                layout (location = 0) in vec3 aPos;
                uniform vec2 uOffset;
                uniform vec2 uScale;
                uniform float uRotZ; // Rotación en radianes
                out vec2 vPos; // Pasamos la posición original al Fragment Shader
                void main() {
                    vPos = vec2(aPos.x, aPos.y);

                    // Rotar los vértices locales
                    float c = cos(uRotZ);
                    float s = sin(uRotZ);
                    float xRot = aPos.x * c - aPos.y * s;
                    float yRot = aPos.x * s + aPos.y * c;

                    vec2 finalPos = vec2(xRot, yRot) * uScale + uOffset;
                    gl_Position = vec4(finalPos, aPos.z, 1.0);
                }
                """;

        // Es el Fragment Shader: Pinta el pixel (soporta colores solidos o degradados)
        String fragmentSrc = """
                #version 330 core
                in vec2 vPos;
                uniform vec3 uColor; // Color base o color superior
                uniform vec3 uColorBottom; // Color inferior para el degradado
                uniform bool uUseGradient;
                out vec4 fragColor;
                void main() {
                    if (uUseGradient) {
                        // vPos.y va de -0.5 (abajo) a 0.5 (arriba)
                        float factor = vPos.y + 0.5; // Normalizamos a 0.0 - 1.0
                        fragColor = vec4(mix(uColorBottom, uColor, factor), 1.0);
                    } else {
                        fragColor = vec4(uColor, 1.0);
                    }
                }
                """;

        int vertexShader = compilarShader(vertexSrc, GL20.GL_VERTEX_SHADER);
        int fragmentShader = compilarShader(fragmentSrc, GL20.GL_FRAGMENT_SHADER);

        programa = GL20.glCreateProgram();
        GL20.glAttachShader(programa, vertexShader);
        GL20.glAttachShader(programa, fragmentShader);
        GL20.glLinkProgram(programa);

        // Guardamos las ubicaciones de los Uniforms para poder cambiarlos luego
        uOffsetLocation = GL20.glGetUniformLocation(programa, "uOffset");
        uScaleLocation = GL20.glGetUniformLocation(programa, "uScale");
        uColorLocation = GL20.glGetUniformLocation(programa, "uColor");
        uRotZLocation = GL20.glGetUniformLocation(programa, "uRotZ");
        uUseGradientLocation = GL20.glGetUniformLocation(programa, "uUseGradient");
        uColorBottomLocation = GL20.glGetUniformLocation(programa, "uColorBottom");

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    private int compilarShader(String codigo, int tipo) {
        int shader = GL20.glCreateShader(tipo);
        GL20.glShaderSource(shader, codigo);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error compilando shader: " + GL20.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    private void crearQuadBase() {
        // Un cuadrado perfecto de 1x1 centrado en (0,0)
        float[] vertices = {
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.5f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, 0.5f, 0.0f,
                -0.5f, 0.5f, 0.0f
        };

        vaoQuad = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoQuad);

        vboQuad = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboQuad);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void crearTrianguloBase() {
        // Un triángulo equilatero (tambien isosceles) base apuntando hacia la derecha
        float[] vertices = {
                0.5f, 0.0f, 0.0f, // Punta derecha
                -0.5f, 0.5f, 0.0f, // Arriba izquierda
                -0.5f, -0.5f, 0.0f // Abajo izquierda
        };

        vaoTri = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoTri);

        vboTri = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTri);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    /**
     * Genera un circulo unitario usando triangulos.
     * Centro en (0,0), radio 0.5. Se generan muchas rebanadas (CIRCLE_SEGMENTS) 
     * Cada rebanada = 3 vertices: Centro, Punto A del borde, Punto B del borde.
     */
    private void crearCirculoBase() {
        // Cada segmento necesita 3 vértices (Centro + 2 puntos del borde)
        int totalVertices = CIRCLE_SEGMENTS * 3;
        float[] vertices = new float[totalVertices * 3]; // X, Y, Z por vértice
        int idx = 0;
        for (int i = 0; i < CIRCLE_SEGMENTS; i++) {
            double anguloA = 2.0 * Math.PI * i / CIRCLE_SEGMENTS;
            double anguloB = 2.0 * Math.PI * (i + 1) / CIRCLE_SEGMENTS;
            // Vértice 1: Centro del círculo
            vertices[idx++] = 0.0f;
            vertices[idx++] = 0.0f;
            vertices[idx++] = 0.0f;
            // Vértice 2: Punto A en el borde (radio 0.5)
            vertices[idx++] = (float) Math.cos(anguloA) * 0.5f;
            vertices[idx++] = (float) Math.sin(anguloA) * 0.5f;
            vertices[idx++] = 0.0f;
            // Vértice 3: Punto B en el borde (radio 0.5)
            vertices[idx++] = (float) Math.cos(anguloB) * 0.5f;
            vertices[idx++] = (float) Math.sin(anguloB) * 0.5f;
            vertices[idx++] = 0.0f;
        }

        vaoCircle = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoCircle);

        vboCircle = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboCircle);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    /**
     * Dibuja un círculo (elipse si ancho != alto) con rotación opcional.
     */
    public void dibujarCirculo(float x, float y, float diametro, float r, float g, float b) {
        dibujarFigura(vaoCircle, CIRCLE_SEGMENTS * 3, x, y, diametro, diametro, r, g, b, 0.0f);
    }

    /**
     * Dibuja una elipse (círculo achatado) con rotación en grados.
     */
    public void dibujarEllipse(float x, float y, float anchoD, float altoD, float r, float g, float b,
            float rotGrados) {
        float rotRad = (float) Math.toRadians(rotGrados);
        dibujarFigura(vaoCircle, CIRCLE_SEGMENTS * 3, x, y, anchoD, altoD, r, g, b, rotRad);
    }

    /**
     * Dibuja un rectángulo sin rotación.
     */
    public void dibujarRectangulo(float x, float y, float ancho, float alto, float r, float g, float b) {
        dibujarFigura(vaoQuad, 6, x, y, ancho, alto, r, g, b, 0.0f);
    }

    /**
     * Dibuja un rectángulo con rotación (en grados).
     */
    public void dibujarRectanguloRotado(float x, float y, float ancho, float alto, float r, float g, float b,
            float rotacionGrados) {
        float rotRadianes = (float) Math.toRadians(rotacionGrados);
        dibujarFigura(vaoQuad, 6, x, y, ancho, alto, r, g, b, rotRadianes);
    }

    /**
     * Dibuja un triángulo con rotación (en grados).
     */
    public void dibujarTriangulo(float x, float y, float ancho, float alto, float r, float g, float b,
            float rotacionGrados) {
        float rotRadianes = (float) Math.toRadians(rotacionGrados);
        dibujarFigura(vaoTri, 3, x, y, ancho, alto, r, g, b, rotRadianes);
    }

    /**
     * Dibuja un fondo ocupando toda la pantalla con un degradado vertical.
     */
    public void dibujarFondoDegradado(float topR, float topG, float topB, float botR, float botG, float botB) {
        GL20.glUseProgram(programa);
        GL30.glBindVertexArray(vaoQuad);

        GL20.glUniform2f(uOffsetLocation, 0.0f, 0.0f);
        GL20.glUniform2f(uScaleLocation, 2.0f, 2.0f); // Ocupa todo el espacio de -1 a 1

        GL20.glUniform3f(uColorLocation, topR, topG, topB);
        GL20.glUniform3f(uColorBottomLocation, botR, botG, botB);
        GL20.glUniform1i(uUseGradientLocation, 1); // Activamos el modo degradado
        GL20.glUniform1f(uRotZLocation, 0.0f);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);

        GL20.glUniform1i(uUseGradientLocation, 0); // Lo desactivamos al terminar
    }

    private void dibujarFigura(int targetVao, int count, float x, float y, float ancho, float alto, float r, float g,
            float b, float rotRadianes) {
        GL20.glUseProgram(programa);
        GL30.glBindVertexArray(targetVao);

        // Pasamos los valores al shader (Modo Sólido por defecto)
        GL20.glUniform1i(uUseGradientLocation, 0);
        GL20.glUniform2f(uOffsetLocation, x, y);
        GL20.glUniform2f(uScaleLocation, ancho, alto);
        GL20.glUniform3f(uColorLocation, r, g, b);
        GL20.glUniform1f(uRotZLocation, rotRadianes);

        // ¡Dibuja!
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, count);
    }

    public void cleanup() {
        GL30.glDeleteVertexArrays(vaoQuad);
        GL30.glDeleteVertexArrays(vaoTri);
        GL30.glDeleteVertexArrays(vaoCircle);
        GL15.glDeleteBuffers(vboQuad);
        GL15.glDeleteBuffers(vboTri);
        GL15.glDeleteBuffers(vboCircle);
        GL20.glDeleteProgram(programa);
    }
}
