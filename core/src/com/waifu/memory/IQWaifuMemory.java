package com.waifu.memory;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.waifu.memory.data.PlayerData;
import com.waifu.memory.managers.AssetManager;
import com.waifu.memory.managers.AudioManager;
import com.waifu.memory.managers.SaveManager;
import com.waifu.memory.screens.HomeScreen;
import com.waifu.memory.utils.Constants;

/**
 * Clase principal del juego IQ Waifu Memory
 * Maneja los managers globales y el ciclo de vida
 */
public class IQWaifuMemory extends Game {
    
    private SpriteBatch batch;
    private AssetManager assetManager;
    private AudioManager audioManager;
    private SaveManager saveManager;
    
    private PlayerData playerData;
    
    private AdHandler adHandler;
    
    /**
     * Interface para manejo de anuncios
     * Implementada por la plataforma (Android/iOS)
     */
    public interface AdHandler {
        // Rewarded Ads
        boolean isRewardedAdLoaded();
        void showRewardedAd(RewardCallback callback);
        
        // Interstitial Ads
        void showInterstitialAd();
        
        // Banner Ads - NUEVO
        void showBanner();
        void hideBanner();
        boolean isBannerVisible();
    }
    
    public interface RewardCallback {
        void onRewardEarned();
        void onAdFailed();
    }
    
    @Override
    public void create() {
        Gdx.app.log(Constants.TAG, "IQ Waifu Memory v" + Constants.VERSION + " starting...");
        
        batch = new SpriteBatch();
        
        assetManager = new AssetManager();
        audioManager = new AudioManager();
        saveManager = new SaveManager();
        
        assetManager.loadEssentialAssets();
        
        playerData = saveManager.loadPlayerData();
        if (playerData == null) {
            Gdx.app.log(Constants.TAG, "Creating new player data");
            playerData = new PlayerData();
            saveManager.savePlayerData(playerData);
        }
        
        audioManager.setMusicVolume(playerData.musicVolume);
        audioManager.setSfxVolume(playerData.sfxVolume);
        
        setScreen(new HomeScreen(this));
        
        Gdx.app.log(Constants.TAG, "Game initialized successfully");
    }
    
    public void setAdHandler(AdHandler handler) {
        this.adHandler = handler;
        Gdx.app.log(Constants.TAG, "Ad handler configured");
    }
    
    public boolean hasAdHandler() {
        return adHandler != null;
    }
    
    public AdHandler getAdHandler() {
        return adHandler;
    }
    
    public void savePlayerData() {
        if (playerData != null && saveManager != null) {
            saveManager.savePlayerData(playerData);
        }
    }
    
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
    
    public PlayerData getPlayerData() {
        return playerData;
    }
    
    @Override
    public void dispose() {
        Gdx.app.log(Constants.TAG, "Disposing game resources...");
        
        savePlayerData();
        
        if (batch != null) batch.dispose();
        if (assetManager != null) assetManager.dispose();
        if (audioManager != null) audioManager.dispose();
        
        if (getScreen() != null) {
            getScreen().dispose();
        }
        
        Gdx.app.log(Constants.TAG, "Game disposed");
    }
}