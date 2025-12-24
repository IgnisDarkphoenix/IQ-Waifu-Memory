package com.waifu.memory.data;

import com.waifu.memory.utils.Constants;

/**
 * Datos de configuración para un nivel específico
 * Define grid, modificadores, tiempo y pool de personajes
 */
public class LevelData {
    
    // Identificación
    public final int levelNumber;
    public final Difficulty difficulty;
    
    // Configuración del grid
    public final int gridSize;
    public final int totalPairs;
    
    // Tiempo
    public final int baseTimeBonus;  // Bonus adicional al tiempo base del jugador
    
    // Modificadores
    public final boolean shuffleEnabled;
    public final int shuffleInterval;
    public final boolean multiGridEnabled;
    public final int multiGridCount;
    public final boolean fadeEnabled;
    
    // Recompensas
    public final float rewardMultiplier;
    
    // Pool de personajes permitidos para este nivel
    public final int[] characterPool;
    
    public enum Difficulty {
        EASY, NORMAL, HARD
    }
    
    /**
     * Constructor privado - usar LevelDataBuilder o create()
     */
    private LevelData(int levelNumber, Difficulty difficulty, int gridSize,
                      int baseTimeBonus, boolean shuffleEnabled, int shuffleInterval,
                      boolean multiGridEnabled, int multiGridCount, boolean fadeEnabled,
                      float rewardMultiplier, int[] characterPool) {
        this.levelNumber = levelNumber;
        this.difficulty = difficulty;
        this.gridSize = gridSize;
        this.totalPairs = (gridSize * gridSize) / 2;
        this.baseTimeBonus = baseTimeBonus;
        this.shuffleEnabled = shuffleEnabled;
        this.shuffleInterval = shuffleInterval;
        this.multiGridEnabled = multiGridEnabled;
        this.multiGridCount = multiGridCount;
        this.fadeEnabled = fadeEnabled;
        this.rewardMultiplier = rewardMultiplier;
        this.characterPool = characterPool;
    }
    
    /**
     * Crea los datos de nivel basándose en el número de nivel
     */
    public static LevelData create(int levelNumber) {
        Difficulty difficulty;
        int gridSize;
        int baseTimeBonus;
        boolean shuffle = false;
        int shuffleInterval = Constants.SHUFFLE_INTERVAL;
        boolean multiGrid = false;
        int multiGridCount = 1;
        boolean fade = false;
        float multiplier;
        
        // ========== NIVELES FÁCILES (1-33) ==========
        if (levelNumber <= Constants.LEVELS_EASY_END) {
            difficulty = Difficulty.EASY;
            gridSize = Constants.GRID_EASY;
            baseTimeBonus = Constants.TIME_BONUS_4X4;
            multiplier = Constants.MULTIPLIER_EASY;
            
            // Sin modificadores en fácil
            shuffle = false;
            
        // ========== NIVELES NORMALES (34-66) ==========
        } else if (levelNumber <= Constants.LEVELS_NORMAL_END) {
            difficulty = Difficulty.NORMAL;
            gridSize = Constants.GRID_NORMAL;
            baseTimeBonus = Constants.TIME_BONUS_6X6;
            multiplier = Constants.MULTIPLIER_NORMAL;
            
            // Shuffle activado desde nivel 45
            shuffle = levelNumber >= 45;
            
        // ========== NIVELES DIFÍCILES (67-100) ==========
        } else {
            difficulty = Difficulty.HARD;
            gridSize = Constants.GRID_HARD;
            baseTimeBonus = Constants.TIME_BONUS_8X8;
            multiplier = Constants.MULTIPLIER_HARD;
            
            // Shuffle siempre activo
            shuffle = true;
            
            // Multi-grid desde nivel 78
            if (levelNumber >= 78) {
                multiGrid = true;
                multiGridCount = 2;
            }
            
            // Fade desde nivel 89
            if (levelNumber >= 89) {
                fade = true;
            }
            
            // Nivel 96+: todo activado, 3 grids
            if (levelNumber >= 96) {
                multiGridCount = 3;
            }
        }
        
        // Generar pool de personajes para este nivel
        int[] pool = generateCharacterPool(levelNumber, gridSize);
        
        return new LevelData(
            levelNumber, difficulty, gridSize,
            baseTimeBonus, shuffle, shuffleInterval,
            multiGrid, multiGridCount, fade,
            multiplier, pool
        );
    }
    
    /**
     * Genera el pool de personajes disponibles para un nivel
     * Distribuye los 50 personajes a lo largo de los niveles
     */
    private static int[] generateCharacterPool(int levelNumber, int gridSize) {
        int pairsNeeded = (gridSize * gridSize) / 2;
        
        // Calcular cuántos personajes desbloquear basado en progreso
        // Nivel 1: 8 personajes, Nivel 100: todos los 50
        int unlockedCharacters = Math.min(
            Constants.TOTAL_CHARACTERS,
            8 + (levelNumber * 42 / 100)
        );
        
        // Asegurar que tenemos suficientes para el grid
        unlockedCharacters = Math.max(unlockedCharacters, pairsNeeded);
        unlockedCharacters = Math.min(unlockedCharacters, Constants.TOTAL_CHARACTERS);
        
        // Crear pool con los personajes desbloqueados
        int[] pool = new int[unlockedCharacters];
        for (int i = 0; i < unlockedCharacters; i++) {
            pool[i] = i;
        }
        
        return pool;
    }
    
    /**
     * Calcula el tiempo total para este nivel dado el nivel de mejora del jugador
     */
    public int calculateTotalTime(int playerTimeLevel) {
        int baseTime = Constants.TIME_VALUES[playerTimeLevel];
        return baseTime + baseTimeBonus;
    }
    
    /**
     * Verifica si un personaje está en el pool de este nivel
     */
    public boolean isCharacterInPool(int characterId) {
        for (int id : characterPool) {
            if (id == characterId) return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return String.format(
            "Level %d [%s] - Grid: %dx%d, Shuffle: %s, MultiGrid: %s(%d), Fade: %s, Multiplier: %.1fx",
            levelNumber, difficulty, gridSize, gridSize,
            shuffleEnabled, multiGridEnabled, multiGridCount, fadeEnabled, rewardMultiplier
        );
    }
}