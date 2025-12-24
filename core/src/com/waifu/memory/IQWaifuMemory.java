package com.waifu.memory;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.waifu.memory.managers.AssetManager;
import com.waifu.memory.managers.AudioManager;
import com.waifu.memory.managers.SaveManager;
import com.waifu.memory.screens.SplashScreen;
import com.waifu.memory.utils.Constants;

/**
 * Clase principal del juego IQ Waifu Memory
 * Extiende Game para manejar múltiples pantallas
 */
public class IQWaifuMemory extends Game {
    
    // SpriteBatch compartido para optimización
    private SpriteBatch batch;
    
    // Managers del juego
    private AssetManager assetManager;
    private AudioManager audioManager;
    private SaveManager saveManager;
    
    // Interface para ads (implementada en Android)
    private AdHandler adHandler;
    
    // Interfaz para manejar anuncios
    public interface AdHandler {
        void showRewardedAd(RewardCallback callback);
        void showInterstitialAd();
        boolean isRewardedAdLoaded();
        void loadRewardedAd();
    }
    
    // Callback para recompensas de anuncios
    public interface RewardCallback {
        void onRewardEarned();
        void onAdFailed();
    }
    
    public IQWaifuMemory() {
        this.adHandler = null;
    }
    
    public IQWaifuMemory(AdHandler adHandler) {
        this.adHandler = adHandler;
    }
    
    @Override
    public void create() {
        Gdx.app.log(Constants.TAG, "Iniciando IQ Waifu Memory v" + Constants.VERSION);
        
        // Inicializar SpriteBatch
        batch = new SpriteBatch();
        
        // Inicializar managers
        assetManager = new AssetManager();
        audioManager = new AudioManager();
        saveManager = new SaveManager();
        
        // Cargar datos guardados
        saveManager.load();
        
        // Ir a la pantalla de splash
        setScreen(new SplashScreen(this));
    }
    
    @Override
    public void render() {
        super.render();
    }
    
    @Override
    public void dispose() {
        Gdx.app.log(Constants.TAG, "Cerrando juego...");
        
        if (batch != null) {
            batch.dispose();
        }
        
        if (assetManager != null) {
            assetManager.dispose();
        }
        
        if (audioManager != null) {
            audioManager.dispose();
        }
        
        // Guardar antes de cerrar
        if (saveManager != null) {
            saveManager.save();
        }
    }
    
    // Getters para acceso desde otras clases
    public SpriteBatch getBatch() {
        return batch;
    }
    
    public AssetManager getAssetManager() {
        return assetManager;
    }
    
    public AudioManager getAudioManager() {
        return audioManager;
    }
    
    public SaveManager getSaveManager() {
        return saveManager;
    }
    
    public AdHandler getAdHandler() {
        return adHandler;
    }
    
    public boolean hasAdHandler() {
        return adHandler != null;
    }
}