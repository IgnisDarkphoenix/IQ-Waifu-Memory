package com.waifu.memory;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.waifu.memory.utils.Constants;

public class AndroidLauncher extends AndroidApplication implements IQWaifuMemory.AdHandler {

    private static final String REWARDED_AD_ID = "ca-app-pub-3940256099942544/5224354917";
    private static final String INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712";

    private RewardedAd rewardedAd;
    private InterstitialAd interstitialAd;

    private IQWaifuMemory gameInstance;
    private boolean exitPendingFinish = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        config.useAccelerometer = false;
        config.useCompass = false;

        MobileAds.initialize(this, status -> {
            loadRewardedAd();
            loadInterstitialAd();
        });

        gameInstance = new IQWaifuMemory(this);
        initialize(gameInstance, config);
    }

    @Override
    public void loadRewardedAd() {
        runOnUiThread(() -> {
            AdRequest adRequest = new AdRequest.Builder().build();
            RewardedAd.load(this, REWARDED_AD_ID, adRequest, new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(RewardedAd ad) {
                    rewardedAd = ad;
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    rewardedAd = null;
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
        runOnUiThread(() -> {
            if (rewardedAd == null) {
                if (callback != null) callback.onAdFailed();
                return;
            }

            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    rewardedAd = null;
                    loadRewardedAd();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    rewardedAd = null;
                    loadRewardedAd();
                    if (callback != null) callback.onAdFailed();
                }
            });

            rewardedAd.show(this, rewardItem -> {
                if (callback != null) callback.onRewardEarned();
            });
        });
    }

    private void loadInterstitialAd() {
        runOnUiThread(() -> {
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(this, INTERSTITIAL_AD_ID, adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(InterstitialAd ad) {
                    interstitialAd = ad;
                }

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    interstitialAd = null;
                }
            });
        });
    }

    @Override
    public void showInterstitialAd() {
        runOnUiThread(() -> {
            if (interstitialAd == null) {
                loadInterstitialAd();
                return;
            }

            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    interstitialAd = null;
                    loadInterstitialAd();

                    if (exitPendingFinish) {
                        exitPendingFinish = false;
                        finish();
                    }
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    interstitialAd = null;
                    loadInterstitialAd();

                    if (exitPendingFinish) {
                        exitPendingFinish = false;
                        finish();
                    }
                }
            });

            interstitialAd.show(this);
        });
    }

    @Override
    public void onBackPressed() {
        try {
            if (Constants.INTERSTITIAL_ON_EXIT_ENABLED &&
                gameInstance != null &&
                gameInstance.getSaveManager() != null &&
                gameInstance.getSaveManager().getPlayerData() != null &&
                gameInstance.getSaveManager().getPlayerData().shouldShowExitInterstitial()) {

                if (interstitialAd != null) {
                    exitPendingFinish = true;
                    showInterstitialAd();
                    gameInstance.getSaveManager().getPlayerData().recordInterstitialShown();
                    gameInstance.getSaveManager().save();
                    return;
                }
            }
        } catch (Exception ignored) {
        }
        super.onBackPressed();
    }
}