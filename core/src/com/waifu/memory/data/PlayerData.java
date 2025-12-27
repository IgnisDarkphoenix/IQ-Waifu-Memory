package com.waifu.memory.data;

import com.waifu.memory.utils.Constants;
import java.util.HashMap;
import java.util.Map;

public class PlayerData {

    public int pcoins;
    public int totalPcoinsEarned;

    public int currentLevel;
    public int maxLevelCompleted;

    public int pairValueLevel;
    public int timeBonusLevel;

    public Map<String, Boolean> galleryUnlocks;

    public float musicVolume;
    public float sfxVolume;
    public String language;

    public int totalGamesPlayed;
    public int totalPairsFound;
    public int totalVictories;
    public int totalDefeats;
    public int currentWinStreak;
    public int bestWinStreak;

    public long lastDailyRewardTime;
    public int dailyRewardStreak;

    public boolean tutorialCompleted;

    public int gamesPlayedSinceLastAd;

    public int totalRewardedAdsWatched;
    public int totalInterstitialsShown;
    public int totalHintsUsed;
    public int totalHdDownloads;

    public PlayerData() {
        pcoins = Constants.INITIAL_PCOINS;
        totalPcoinsEarned = 0;

        currentLevel = 1;
        maxLevelCompleted = 0;

        pairValueLevel = 0;
        timeBonusLevel = 0;

        galleryUnlocks = new HashMap<>();

        musicVolume = Constants.DEFAULT_MUSIC_VOLUME;
        sfxVolume = Constants.DEFAULT_SFX_VOLUME;
        language = "es";

        totalGamesPlayed = 0;
        totalPairsFound = 0;
        totalVictories = 0;
        totalDefeats = 0;
        currentWinStreak = 0;
        bestWinStreak = 0;

        lastDailyRewardTime = 0;
        dailyRewardStreak = 0;

        tutorialCompleted = false;

        gamesPlayedSinceLastAd = 0;

        totalRewardedAdsWatched = 0;
        totalInterstitialsShown = 0;
        totalHintsUsed = 0;
        totalHdDownloads = 0;
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    public int getCurrentPairValue() {
        int idx = clamp(pairValueLevel, 0, Constants.PAIR_VALUES.length - 1);
        return Constants.PAIR_VALUES[idx];
    }

    public boolean canUpgradePairValue() {
        if (pairValueLevel >= Constants.MAX_PAIR_LEVEL) return false;
        int next = pairValueLevel + 1;
        if (next < 0 || next >= Constants.PAIR_UPGRADE_COSTS.length) return false;
        return pcoins >= Constants.PAIR_UPGRADE_COSTS[next];
    }

    public int getNextPairUpgradeCost() {
        if (pairValueLevel >= Constants.MAX_PAIR_LEVEL) return -1;
        int next = pairValueLevel + 1;
        if (next < 0 || next >= Constants.PAIR_UPGRADE_COSTS.length) return -1;
        return Constants.PAIR_UPGRADE_COSTS[next];
    }

    public boolean upgradePairValue() {
        if (!canUpgradePairValue()) return false;
        pcoins -= Constants.PAIR_UPGRADE_COSTS[pairValueLevel + 1];
        pairValueLevel++;
        return true;
    }

    public int getCurrentBaseTime() {
        int idx = clamp(timeBonusLevel, 0, Constants.TIME_VALUES.length - 1);
        return Constants.TIME_VALUES[idx];
    }

    public boolean canUpgradeTime() {
        if (timeBonusLevel >= Constants.MAX_TIME_LEVEL) return false;
        int next = timeBonusLevel + 1;
        if (next < 0 || next >= Constants.TIME_UPGRADE_COSTS.length) return false;
        return pcoins >= Constants.TIME_UPGRADE_COSTS[next];
    }

    public int getNextTimeUpgradeCost() {
        if (timeBonusLevel >= Constants.MAX_TIME_LEVEL) return -1;
        int next = timeBonusLevel + 1;
        if (next < 0 || next >= Constants.TIME_UPGRADE_COSTS.length) return -1;
        return Constants.TIME_UPGRADE_COSTS[next];
    }

    public boolean upgradeTime() {
        if (!canUpgradeTime()) return false;
        pcoins -= Constants.TIME_UPGRADE_COSTS[timeBonusLevel + 1];
        timeBonusLevel++;
        return true;
    }

    public void addPcoins(int amount) {
        if (amount <= 0) return;
        pcoins += amount;
        totalPcoinsEarned += amount;
    }

    public boolean spendPcoins(int amount) {
        if (amount <= 0) return false;
        if (pcoins < amount) return false;
        pcoins -= amount;
        return true;
    }

    private String getUnlockKey(int characterId, int variant) {
        return characterId + "_" + variant;
    }

    public boolean isVariantUnlocked(int characterId, int variant) {
        if (galleryUnlocks == null) galleryUnlocks = new HashMap<>();
        String key = getUnlockKey(characterId, variant);
        return galleryUnlocks.getOrDefault(key, false);
    }

    public int getCharacterUnlockLevel(int characterId) {
        int level = 0;
        for (int v = 0; v < Constants.VARIANTS_PER_CHARACTER; v++) {
            if (isVariantUnlocked(characterId, v)) level = v + 1;
            else break;
        }
        return level;
    }

    public int getNextVariantToUnlock(int characterId) {
        for (int v = 0; v < Constants.VARIANTS_PER_CHARACTER; v++) {
            if (!isVariantUnlocked(characterId, v)) return v;
        }
        return -1;
    }

    public int getNextUnlockCost(int characterId) {
        int nextVariant = getNextVariantToUnlock(characterId);
        if (nextVariant < 0) return -1;
        return Constants.getGalleryCost(nextVariant);
    }

    public boolean canUnlockNextVariant(int characterId) {
        int cost = getNextUnlockCost(characterId);
        if (cost < 0) return false;
        return pcoins >= cost;
    }

    public boolean unlockNextVariant(int characterId) {
        int nextVariant = getNextVariantToUnlock(characterId);
        if (nextVariant < 0) return false;

        int cost = Constants.getGalleryCost(nextVariant);
        if (cost < 0 || pcoins < cost) return false;

        pcoins -= cost;
        if (galleryUnlocks == null) galleryUnlocks = new HashMap<>();
        galleryUnlocks.put(getUnlockKey(characterId, nextVariant), true);
        return true;
    }

    public float getGalleryCompletionPercent() {
        int totalPossible = Constants.TOTAL_CHARACTERS * Constants.VARIANTS_PER_CHARACTER;
        if (totalPossible <= 0) return 0f;

        int unlocked = 0;
        if (galleryUnlocks == null) galleryUnlocks = new HashMap<>();
        for (Boolean value : galleryUnlocks.values()) {
            if (value != null && value) unlocked++;
        }
        return (float) unlocked / totalPossible * 100f;
    }

    public boolean canClaimDailyReward() {
        long now = System.currentTimeMillis();
        long day = 24L * 60L * 60L * 1000L;
        return (now - lastDailyRewardTime) >= day;
    }

    public int claimDailyReward() {
        if (!canClaimDailyReward()) return 0;

        long now = System.currentTimeMillis();
        long day = 24L * 60L * 60L * 1000L;
        long twoDays = 2L * day;

        if ((now - lastDailyRewardTime) >= twoDays) {
            dailyRewardStreak = 0;
        }

        int idx = clamp(dailyRewardStreak, 0, Constants.DAILY_REWARDS.length - 1);
        int reward = Constants.DAILY_REWARDS[idx];
        addPcoins(reward);

        dailyRewardStreak = (dailyRewardStreak + 1) % Constants.DAILY_REWARDS.length;
        lastDailyRewardTime = now;

        return reward;
    }

    public void recordGamePlayed(boolean victory, int pairsFound) {
        totalGamesPlayed++;
        totalPairsFound += Math.max(0, pairsFound);

        if (totalGamesPlayed <= Constants.INTERSTITIAL_NEW_PLAYER_GRACE_GAMES) {
            gamesPlayedSinceLastAd = 0;
        } else {
            gamesPlayedSinceLastAd++;
        }

        if (victory) {
            totalVictories++;
            currentWinStreak++;
            if (currentWinStreak > bestWinStreak) bestWinStreak = currentWinStreak;
        } else {
            totalDefeats++;
            currentWinStreak = 0;
        }
    }

    public boolean shouldShowInterstitial() {
        if (!Constants.isInterstitialEligible(totalGamesPlayed)) return false;
        return gamesPlayedSinceLastAd >= Constants.INTERSTITIAL_FREQUENCY;
    }

    public boolean shouldShowExitInterstitial() {
        if (!Constants.INTERSTITIAL_ON_EXIT_ENABLED) return false;
        return Constants.isInterstitialEligible(totalGamesPlayed);
    }

    public void resetAdCounter() {
        gamesPlayedSinceLastAd = 0;
    }

    public void recordInterstitialShown() {
        totalInterstitialsShown++;
        resetAdCounter();
    }

    public void recordRewardedWatched() {
        totalRewardedAdsWatched++;
    }

    public void recordHintUsed() {
        totalHintsUsed++;
    }

    public void recordHdDownload() {
        totalHdDownloads++;
    }

    @Override
    public String toString() {
        return String.format(
            "PlayerData[PCOINS=%d, Level=%d/%d, PairLv=%d, TimeLv=%d, Gallery=%.1f%%, Games=%d]",
            pcoins, currentLevel, maxLevelCompleted, pairValueLevel, timeBonusLevel,
            getGalleryCompletionPercent(), totalGamesPlayed
        );
    }
}