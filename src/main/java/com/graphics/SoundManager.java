package com.graphics;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Gestor de sonido sintetizado estilo 8-bits.
 * Usa ondas senoidales matemáticas para evitar dependencias de archivos externos.
 * Cumple con la regla de Separación de Responsabilidades y Agnosticismo.
 */
public class SoundManager {

    // Usamos un pool de hilos para reproducir sonidos de forma asíncrona sin bloquear el juego
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Efecto de Salto: Tono rápido ascendente
     */
    public static void playJumpSound() {
        executor.submit(() -> generateTone(300, 600, 150));
    }

    /**
     * Efecto de Punto: Dos tonos agudos cortos (Estilo moneda)
     */
    public static void playPointSound() {
        executor.submit(() -> {
            generateTone(1200, 1200, 80);
            try { Thread.sleep(20); } catch (InterruptedException ignored) {}
            generateTone(1600, 1600, 100);
        });
    }

    /**
     * Efecto de Choque: Tono grave descendente
     */
    public static void playCrashSound() {
        executor.submit(() -> generateTone(150, 50, 400));
    }

    /**
     * Genera y reproduce una onda de sonido mediante la API nativa de Java.
     * 
     * @param startFreq Frecuencia inicial en Hz
     * @param endFreq   Frecuencia final en Hz (para crear efectos de "deslizamiento")
     * @param durationMs Duración del sonido en milisegundos
     */
    private static void generateTone(double startFreq, double endFreq, int durationMs) {
        try {
            float sampleRate = 44100; // Calidad CD
            AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            sdl.start();

            int length = (int) (sampleRate * durationMs / 1000.0);
            byte[] buf = new byte[length];
            double phase = 0;
            
            // Generación matemática de la onda
            for (int i = 0; i < length; i++) {
                double currentFreq = startFreq + (endFreq - startFreq) * ((double) i / length);
                phase += 2.0 * Math.PI * currentFreq / sampleRate;
                // Multiplicamos por 40.0 para controlar el volumen (máx es 127)
                buf[i] = (byte) (Math.sin(phase) * 40.0); 
            }
            
            sdl.write(buf, 0, length);
            sdl.drain();
            sdl.stop();
            sdl.close();
        } catch (Exception e) {
            // Falla silenciosamente si la computadora no tiene salida de audio
            System.err.println("Advertencia: No se pudo reproducir el sonido.");
        }
    }

    /**
     * Apaga el motor de sonido (Limpieza de recursos)
     */
    public static void shutdown() {
        executor.shutdownNow();
    }
}
