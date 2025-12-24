package com.waifu.memory.data;

import com.waifu.memory.utils.Constants;
import java.util.HashMap;
import java.util.Map;

/**
 * Datos del jugador - Persistencia completa
 */
public class PlayerData {
    
    // ========== ECONOMÍA ==========
    public int pcoins;
    public int totalPcoinsEarned;
    
    // ========== PROGRESIÓN ==========
    public int currentLevel;
    public int maxLevelCompleted;
    
    // ========== MEJORAS ==========
    public int pairValueLevel;  // 0-9
    public int timeBonusLevel;  // 0-6
    
    // ========== GALERÍA ==========
    // Mapa: "X_Y" -> true (X=characterId, Y=variant desbloqueado)
    // Ejemplo: "0_0"=true significa personaje 0, variante base desbloqueada
    public Map<String, Boolean> galleryUnlocks;
    
    // ========== CONFIGURACIÓN ==========
    public float musicVolume;
    public float sfxVolume;
    public String language;
    
    // ========== ESTADÍSTICAS ==========
    public int totalGamesPlayed;
    public int totalPairsFound;
    public int totalVictories;
    public int totalDefeats;
    public int currentWinStreak;
    public int bestWinStreak;
    
    // ========== DAILY REWARD ==========
    public long lastDailyRewardTime;
    public int dailyRewardStreak;
    
    // ========== TUTORIAL ==========
    public boolean tutorialCompleted;
    
    // ========== ADS ==========
    public int gamesPlayedSinceLastAd;
    
    public PlayerData() {
        // Economía
        pcoins = Constants.INITIAL_PCOINS;
        totalPcoinsEarned = 0;
        
        // Progresión
        currentLevel = 1;
        maxLevelCompleted = 0;
        
        // Mejoras
        pairValueLevel = 0;
        timeBonusLevel = 0;
        
        // Galería
        galleryUnlocks = new HashMap<>();
        
        // Configuración
        musicVolume = Constants.DEFAULT_MUSIC_VOLUME;
        sfxVolume = Constants.DEFAULT_SFX_VOLUME;
        language = "es";
        
        // Estadísticas
        totalGamesPlayed = 0;
        totalPairsFound = 0;
        totalVictories = 0;
        totalDefeats = 0;
        currentWinStreak = 0;
        bestWinStreak = 0;
        
        // Daily reward
        lastDailyRewardTime = 0;
        dailyRewardStreak = 0;
        
        // Tutorial
        tutorialCompleted = false;
        
        // Ads
        gamesPlayedSinceLastAd = 0;
    }
    
    // ══════════════════════════════════════════════════════════
    // MEJORAS - VALOR DE PAR
    // ══════════════════════════════════════════════════════════
    
    public int getCurrentPairValue() {
        return Constants.PAIR_VALUES[pairValueLevel];
    }
    
    public boolean canUpgradePairValue() {
        if (pairValueLevel >= Constants.MAX_PAIR_LEVEL) return false;
        return pcoins >= Constants.PAIR_UPGRADE_COSTS[pairValueLevel + 1];
    }
    
    public int getNextPairUpgradeCost() {
        if (pairValueLevel >= Constants.MAX_PAIR_LEVEL) return -1;
        return Constants.PAIR_UPGRADE_COSTS[pairValueLevel + 1];
    }
    
    public boolean upgradePairValue() {
        if (!canUpgradePairValue()) return false;
        pcoins -= Constants.PAIR_UPGRADE_COSTS[pairValueLevel + 1];
        pairValueLevel++;
        return true;
    }
    
    // ══════════════════════════════════════════════════════════
    // MEJORAS - TIEMPO
    // ══════════════════════════════════════════════════════════
    
    public int getCurrentBaseTime() {
        return Constants.TIME_VALUES[timeBonusLevel];
    }
    
    public boolean canUpgradeTime() {
        if (timeBonusLevel >= Constants.MAX_TIME_LEVEL) return false;
        return pcoins >= Constants.TIME_UPGRADE_COSTS[timeBonusLevel + 1];
    }
    
    public int getNextTimeUpgradeCost() {
        if (timeBonusLevel >= Constants.MAX_TIME_LEVEL) return -1;
        return Constants.TIME_UPGRADE_COSTS[timeBonusLevel + 1];
    }
    
    public boolean upgradeTime() {
        if (!canUpgradeTime()) return false;
        pcoins -= Constants.TIME_UPGRADE_COSTS[timeBonusLevel + 1];
        timeBonusLevel++;
        return true;
    }
    
    // ══════════════════════════════════════════════════════════
    // ECONOMÍA
    // ══════════════════════════════════════════════════════════
    
    public void addPcoins(int amount) {
        if (amount > 0) {
            pcoins += amount;
            totalPcoinsEarned += amount;
        }
    }
    
    public boolean spendPcoins(int amount) {
        if (pcoins < amount) return false;
        pcoins -= amount;
        return true;
    }
    
    // ══════════════════════════════════════════════════════════
    // GALERÍA - SISTEMA DE DESBLOQUEO POR VARIANTE
    // ══════════════════════════════════════════════════════════
    
    /**
     * Genera la clave para el mapa de desbloqueos
     */
    private String getUnlockKey(int characterId, int variant) {
        return characterId + "_" + variant;
    }
    
    /**
     * Verifica si una variante específica está desbloqueada
     */
    public boolean isVariantUnlocked(int characterId, int variant) {
        String key = getUnlockKey(characterId, variant);
        return galleryUnlocks.getOrDefault(key, false);
    }
    
    /**
     * Obtiene el nivel de desbloqueo de un personaje (0-4)
     * 0 = nada desbloqueado
     * 1 = base desbloqueada
     * 2 = base + ☆
     * 3 = base + ☆ + ☆☆
     * 4 = todo desbloqueado
     */
    public int getCharacterUnlockLevel(int characterId) {
        int level = 0;
        for (int v = 0; v < Constants.VARIANTS_PER_CHARACTER; v++) {
            if (isVariantUnlocked(characterId, v)) {
                level = v + 1;
            } else {
                break; // Las variantes deben desbloquearse en orden
            }
        }
        return level;
    }
    
    /**
     * Obtiene la siguiente variante a desbloquear (-1 si todas están desbloqueadas)
     */
    public int getNextVariantToUnlock(int characterId) {
        for (int v = 0; v < Constants.VARIANTS_PER_CHARACTER; v++) {
            if (!isVariantUnlocked(characterId, v)) {
                return v;
            }
        }
        return -1; // Todas desbloqueadas
    }
    
    /**
     * Obtiene el costo para desbloquear la siguiente variante
     */
    public int getNextUnlockCost(int characterId) {
        int nextVariant = getNextVariantToUnlock(characterId);
        if (nextVariant < 0) return -1;
        return Constants.getGalleryCost(nextVariant);
    }
    
    /**
     * Verifica si puede desbloquear la siguiente variante
     */
    public boolean canUnlockNextVariant(int characterId) {
        int cost = getNextUnlockCost(characterId);
        if (cost < 0) return false;
        return pcoins >= cost;
    }
    
    /**
     * Desbloquea la siguiente variante de un personaje
     * @return true si se desbloqueó exitosamente
     */
    public boolean unlockNextVariant(int characterId) {
        int nextVariant = getNextVariantToUnlock(characterId);
        if (nextVariant < 0) return false;
        
        int cost = Constants.getGalleryCost(nextVariant);
        if (pcoins < cost) return false;
        
        pcoins -= cost;
        String key = getUnlockKey(characterId, nextVariant);
        galleryUnlocks.put(key, true);
        
        return true;
    }
    
    /**
     * Alias para compatibilidad con código existente
     */
    public boolean unlockCharacterLevel(int characterId) {
        return unlockNextVariant(characterId);
    }
    
    /**
     * Calcula el porcentaje de completado de la galería
     */
    public float getGalleryCompletionPercent() {
        int totalPossible = Constants.TOTAL_CHARACTERS * Constants.VARIANTS_PER_CHARACTER;
        int unlocked = 0;
        
        for (Boolean value : galleryUnlocks.values()) {
            if (value != null && value) {
                unlocked++;
            }
        }
        
        return (float) unlocked / totalPossible * 100f;
    }
    
    /**
     * Cuenta cuántos personajes tienen al menos la variante base
     */
    public int getUnlockedCharacterCount() {
        int count = 0;
        for (int c = 0; c < Constants.TOTAL_CHARACTERS; c++) {
            if (isVariantUnlocked(c, 0)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Cuenta cuántos personajes están completamente desbloqueados
     */
    public int getFullyUnlockedCharacterCount() {
        int count = 0;
        for (int c = 0; c < Constants.TOTAL_CHARACTERS; c++) {
            if (getCharacterUnlockLevel(c) >= Constants.VARIANTS_PER_CHARACTER) {
                count++;
            }
        }
        return count;
    }
    
    // ══════════════════════════════════════════════════════════
    // DAILY REWARD
    // ══════════════════════════════════════════════════════════
    
    public boolean canClaimDailyReward() {
        long currentTime = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000;
        return (currentTime - lastDailyRewardTime) >= dayInMillis;
    }
    
    public int claimDailyReward() {
        if (!canClaimDailyReward()) return 0;
        
        long currentTime = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000;
        long twoDaysInMillis = 2 * dayInMillis;
        
        // Reiniciar racha si pasaron más de 2 días
        if ((currentTime - lastDailyRewardTime) >= twoDaysInMillis) {
            dailyRewardStreak = 0;
        }
        
        int reward = Constants.DAILY_REWARDS[dailyRewardStreak];
        addPcoins(reward);
        
        dailyRewardStreak = (dailyRewardStreak + 1) % Constants.DAILY_REWARDS.length;
        lastDailyRewardTime = currentTime;
        
        return reward;
    }
    
    // ══════════════════════════════════════════════════════════
    // ESTADÍSTICAS Y PARTIDAS
    // ══════════════════════════════════════════════════════════
    
    public void recordGamePlayed(boolean victory, int pairsFound) {
        totalGamesPlayed++;
        totalPairsFound += pairsFound;
        gamesPlayedSinceLastAd++;
        
        if (victory) {
            totalVictories++;
            currentWinStreak++;
            if (currentWinStreak > bestWinStreak) {
                bestWinStreak = currentWinStreak;
            }
        } else {
            totalDefeats++;
            currentWinStreak = 0;
        }
    }
    
    public boolean shouldShowInterstitial() {
        return gamesPlayedSinceLastAd >= Constants.INTERSTITIAL_FREQUENCY;
    }
    
    public void resetAdCounter() {
        gamesPlayedSinceLastAd = 0;
    }
    
    // ══════════════════════════════════════════════════════════
    // DEBUG
    // ══════════════════════════════════════════════════════════
    
    @Override
    public String toString() {
        return String.format(
            "PlayerData[PCOINS=%d, Level=%d/%d, PairLv=%d, TimeLv=%d, Gallery=%.1f%%]",
            pcoins, currentLevel, maxLevelCompleted,
            pairValueLevel, timeBonusLevel,
            getGalleryCompletionPercent()
        );
    }
}