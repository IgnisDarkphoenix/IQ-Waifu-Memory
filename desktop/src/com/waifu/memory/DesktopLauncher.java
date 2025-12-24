package com.waifu.memory;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.waifu.memory.utils.Constants;

/**
 * Launcher para pruebas en Desktop (PC)
 * Simula la proporción de pantalla móvil
 */
public class DesktopLauncher {
    
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        
        // Título de la ventana
        config.setTitle(Constants.GAME_TITLE + " - Desktop");
        
        // Tamaño de ventana (proporción móvil 9:16)
        // Escalado para que quepa en pantalla de PC
        int windowHeight = 900;
        int windowWidth = (int) (windowHeight * Constants.ASPECT_RATIO);
        config.setWindowedMode(windowWidth, windowHeight);
        
        // Configuración adicional
        config.setResizable(false);
        config.useVsync(true);
        config.setForegroundFPS(60);
        
        // Iniciar juego sin AdHandler (null) ya que no hay ads en desktop
        new Lwjgl3Application(new IQWaifuMemory(), config);
    }
}