package com.waifu.memory.data;

import com.waifu.memory.utils.Constants;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase que contiene todos los datos del jugador
 * Se serializa/deserializa para guardado
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
    // Mapa: "character_XX" -> nivel desbloqueado (0=bloqueado, 1=base, 2=☆, 3=☆☆, 4=☆☆☆)
    public Map<String, Integer> galleryUnlocks;
    
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
    public long lastDailyRewardTime;  // Timestamp
    public int dailyRewardStreak;     // 0-4
    
    // ========== TUTORIAL ==========
    public boolean tutorialCompleted;
    
    // ========== ADS ==========
    public int gamesPlayedSinceLastAd;
    
    /**
     * Constructor con valores por defecto
     */
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
        language = "es";  // Español por defecto
        
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
    
    // ========== MÉTODOS DE UTILIDAD ==========
    
    /**
     * Obtiene el valor actual de PCOINS por par
     */
    public int getCurrentPairValue() {
        return Constants.PAIR_VALUES[pairValueLevel];
    }
    
    /**
     * Obtiene el tiempo base actual
     */
    public int getCurrentBaseTime() {
        return Constants.TIME_VALUES[timeBonusLevel];
    }
    
    /**
     * Verifica si se puede comprar la siguiente mejora de par
     */
    public boolean canUpgradePairValue() {
        if (pairValueLevel >= Constants.MAX_PAIR_LEVEL) return false;
        return pcoins >= Constants.PAIR_UPGRADE_COSTS[pairValueLevel + 1];
    }
    
    /**
     * Verifica si se puede comprar la siguiente mejora de tiempo
     */
    public boolean canUpgradeTime() {
        if (timeBonusLevel >= Constants.MAX_TIME_LEVEL) return false;
        return pcoins >= Constants.TIME_UPGRADE_COSTS[timeBonusLevel + 1];
    }
    
    /**
     * Obtiene el costo de la siguiente mejora de par
     */
    public int getNextPairUpgradeCost() {
        if (pairValueLevel >= Constants.MAX_PAIR_LEVEL) return -1;
        return Constants.PAIR_UPGRADE_COSTS[pairValueLevel + 1];
    }
    
    /**
     * Obtiene el costo de la siguiente mejora de tiempo
     */
    public int getNextTimeUpgradeCost() {
        if (timeBonusLevel >= Constants.MAX_TIME_LEVEL) return -1;
        return Constants.TIME_UPGRADE_COSTS[timeBonusLevel + 1];
    }
    
    /**
     * Compra mejora de valor de par
     * @return true si la compra fue exitosa
     */
    public boolean upgradePairValue() {
        if (!canUpgradePairValue()) return false;
        
        int cost = Constants.PAIR_UPGRADE_COSTS[pairValueLevel + 1];
        pcoins -= cost;
        pairValueLevel++;
        return true;
    }
    
    /**
     * Compra mejora de tiempo
     * @return true si la compra fue exitosa
     */
    public boolean upgradeTime() {
        if (!canUpgradeTime()) return false;
        
        int cost = Constants.TIME_UPGRADE_COSTS[timeBonusLevel + 1];
        pcoins -= cost;
        timeBonusLevel++;
        return true;
    }
    
    /**
     * Añade PCOINS al jugador
     */
    public void addPcoins(int amount) {
        pcoins += amount;
        totalPcoinsEarned += amount;
    }
    
    /**
     * Gasta PCOINS
     * @return true si tenía suficientes
     */
    public boolean spendPcoins(int amount) {
        if (pcoins < amount) return false;
        pcoins -= amount;
        return true;
    }
    
    /**
     * Obtiene el nivel de desbloqueo de un personaje
     * @param characterId ID del personaje (0-49)
     * @return 0=bloqueado, 1=base, 2=☆, 3=☆☆, 4=☆☆☆
     */
    public int getCharacterUnlockLevel(int characterId) {
        String key = "character_" + characterId;
        return galleryUnlocks.getOrDefault(key, 0);
    }
    
    /**
     * Desbloquea el siguiente nivel de un personaje
     * @return true si el desbloqueo fue exitoso
     */
    public boolean unlockCharacterLevel(int characterId) {
        int currentUnlock = getCharacterUnlockLevel(characterId);
        
        if (currentUnlock >= 4) return false; // Ya está al máximo
        
        // Determinar costo
        int cost;
        switch (currentUnlock) {
            case 0: cost = Constants.GALLERY_BASE_COST; break;
            case 1: cost = Constants.GALLERY_STAR1_COST; break;
            case 2: cost = Constants.GALLERY_STAR2_COST; break;
            case 3: cost = Constants.GALLERY_STAR3_COST; break;
            default: return false;
        }
        
        if (pcoins < cost) return false;
        
        pcoins -= cost;
        String key = "character_" + characterId;
        galleryUnlocks.put(key, currentUnlock + 1);
        
        return true;
    }
    
    /**
     * Calcula el porcentaje de completado de la galería
     */
    public float getGalleryCompletionPercent() {
        int totalUnlocks = 0;
        int maxUnlocks = Constants.TOTAL_CHARACTERS * 4; // 50 personajes x 4 niveles
        
        for (Integer level : galleryUnlocks.values()) {
            totalUnlocks += level;
        }
        
        return (float) totalUnlocks / maxUnlocks * 100f;
    }
    
    /**
     * Verifica si puede reclamar daily reward
     */
    public boolean canClaimDailyReward() {
        long currentTime = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000;
        
        return (currentTime - lastDailyRewardTime) >= dayInMillis;
    }
    
    /**
     * Reclama el daily reward
     * @return cantidad de PCOINS ganados
     */
    public int claimDailyReward() {
        if (!canClaimDailyReward()) return 0;
        
        long currentTime = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000;
        long twoDaysInMillis = 2 * dayInMillis;
        
        // Si pasaron más de 2 días, reiniciar racha
        if ((currentTime - lastDailyRewardTime) >= twoDaysInMillis) {
            dailyRewardStreak = 0;
        }
        
        // Obtener recompensa
        int reward = Constants.DAILY_REWARDS[dailyRewardStreak];
        addPcoins(reward);
        
        // Actualizar racha
        dailyRewardStreak = (dailyRewardStreak + 1) % Constants.DAILY_REWARDS.length;
        lastDailyRewardTime = currentTime;
        
        return reward;
    }
    
    /**
     * Registra una partida jugada
     */
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
    
    /**
     * Verifica si debe mostrar interstitial
     */
    public boolean shouldShowInterstitial() {
        return gamesPlayedSinceLastAd >= Constants.INTERSTITIAL_FREQUENCY;
    }
    
    /**
     * Resetea contador de ads
     */
    public void resetAdCounter() {
        gamesPlayedSinceLastAd = 0;
    }
}