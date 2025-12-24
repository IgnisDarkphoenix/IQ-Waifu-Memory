package com.waifu.memory.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;
import com.waifu.memory.data.PlayerData;
import com.waifu.memory.utils.Constants;

/**
 * Manager para guardar y cargar datos del jugador
 * Usa Preferences de libGDX para persistencia
 */
public class SaveManager {
    
    private static final String PREFS_NAME = "IQWaifuMemorySave";
    private static final String KEY_PLAYER_DATA = "playerData";
    
    private Preferences prefs;
    private PlayerData playerData;
    private Json json;
    
    public SaveManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        json = new Json();
        playerData = new PlayerData();
    }
    
    /**
     * Carga los datos del jugador
     */
    public void load() {
        try {
            String jsonData = prefs.getString(KEY_PLAYER_DATA, "");
            
            if (jsonData.isEmpty()) {
                Gdx.app.log(Constants.TAG, "No se encontró save, creando nuevo...");
                playerData = new PlayerData();
            } else {
                playerData = json.fromJson(PlayerData.class, jsonData);
                Gdx.app.log(Constants.TAG, "Datos cargados: " + playerData.pcoins + " PCOINS");
            }
        } catch (Exception e) {
            Gdx.app.error(Constants.TAG, "Error cargando datos: " + e.getMessage());
            playerData = new PlayerData();
        }
    }
    
    /**
     * Guarda los datos del jugador
     */
    public void save() {
        try {
            String jsonData = json.toJson(playerData);
            prefs.putString(KEY_PLAYER_DATA, jsonData);
            prefs.flush();
            Gdx.app.log(Constants.TAG, "Datos guardados correctamente");
        } catch (Exception e) {
            Gdx.app.error(Constants.TAG, "Error guardando datos: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene los datos del jugador
     */
    public PlayerData getPlayerData() {
        return playerData;
    }
    
    /**
     * Resetea todos los datos (para testing)
     */
    public void resetAll() {
        playerData = new PlayerData();
        save();
        Gdx.app.log(Constants.TAG, "Datos reseteados");
    }
    
    /**
     * Verifica si existe un save previo
     */
    public boolean hasSaveData() {
        return prefs.contains(KEY_PLAYER_DATA);
    }
}