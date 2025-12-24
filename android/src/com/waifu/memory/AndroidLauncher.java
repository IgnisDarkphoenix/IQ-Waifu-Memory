package com.waifu.memory;

import android.os.Bundle;
import android.util.Log;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

/**
 * Launcher de Android para el juego
 * Maneja la integración con AdMob
 */
public class AndroidLauncher extends AndroidApplication implements IQWaifuMemory.AdHandler {
    
    private static final String TAG = "AndroidLauncher";
    
    // IDs de anuncios (USAR IDS DE PRUEBA EN DESARROLLO)
    // Reemplazar con IDs reales antes de publicar
    private static final String REWARDED_AD_ID = "ca-app-pub-3940256099942544/5224354917"; // Test ID
    
    private RewardedAd rewardedAd;
    private IQWaifuMemory.RewardCallback currentRewardCallback;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configuración de libGDX
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        config.useAccelerometer = false;
        config.useCompass = false;
        
        // Inicializar AdMob
        MobileAds.initialize(this, initializationStatus -> {
            Log.d(TAG, "AdMob inicializado");
            loadRewardedAd();
        });
        
        // Iniciar juego con handler de ads
        initialize(new IQWaifuMemory(this), config);
    }
    
    // ========== IMPLEMENTACIÓN DE AdHandler ==========
    
    @Override
    public void loadRewardedAd() {
        runOnUiThread(() -> {
            AdRequest adRequest = new AdRequest.Builder().build();
            
            RewardedAd.load(this, REWARDED_AD_ID, adRequest, 
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedAd ad) {
                        rewardedAd = ad;
                        Log.d(TAG, "Rewarded ad cargado");
                    }
                    
                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        rewardedAd = null;
                        Log.e(TAG, "Error cargando rewarded ad: " + loadAdError.getMessage());
                    }
                });
        });
    }
    
    @Override
    public boolean isRewardedAdLoaded() {
        return rewardedAd != null;
    }
    
    @Override
    public void showRewardedAd(IQWaifuMemory.RewardCallback callback) {
        this.currentRewardCallback = callback;
        
        runOnUiThread(() -> {
            if (rewardedAd != null) {
                rewardedAd.show(this, rewardItem -> {
                    Log.d(TAG, "Usuario ganó recompensa");
                    if (currentRewardCallback != null) {
                        currentRewardCallback.onRewardEarned();
                    }
                    // Cargar siguiente ad
                    loadRewardedAd();
                });
            } else {
                Log.e(TAG, "Rewarded ad no está cargado");
                if (currentRewardCallback != null) {
                    currentRewardCallback.onAdFailed();
                }
            }
        });
    }
    
    @Override
    public void showInterstitialAd() {
        // TODO: Implementar interstitial ads
        Log.d(TAG, "Interstitial ad solicitado");
    }