package com.waifu.memory.utils;

/**
 * Constantes globales del juego
 */
public final class Constants {
    
    private Constants() {} // Prevenir instanciación
    
    // ========== INFO DEL JUEGO ==========
    public static final String TAG = "IQWaifuMemory";
    public static final String VERSION = "1.0.0";
    public static final String GAME_TITLE = "IQ Waifu Memory";
    
    // ========== DIMENSIONES DE PANTALLA ==========
    // Diseño base en portrait (vertical)
    public static final float WORLD_WIDTH = 1080f;
    public static final float WORLD_HEIGHT = 1920f;
    public static final float ASPECT_RATIO = WORLD_WIDTH / WORLD_HEIGHT;
    
    // ========== DIMENSIONES DE CARTAS ==========
    public static final float CARD_SIZE = 200f;      // Tamaño base de carta
    public static final float CARD_PADDING = 10f;    // Espacio entre cartas
    public static final float CARD_FLIP_TIME = 0.3f; // Duración animación flip
    public static final float CARD_SHOW_TIME = 0.8f; // Tiempo mostrando par incorrecto
    
    // ========== TAMAÑOS DE GRID ==========
    public static final int GRID_EASY = 4;    // 4x4 = 16 cartas = 8 pares
    public static final int GRID_NORMAL = 6;  // 6x6 = 36 cartas = 18 pares
    public static final int GRID_HARD = 8;    // 8x8 = 64 cartas = 32 pares
    
    // ========== SISTEMA DE PCOINS ==========
    public static final String CURRENCY_NAME = "PCOINS";
    public static final int INITIAL_PCOINS = 0;
    
    // Valores de par por nivel de mejora (índice = nivel)
    public static final int[] PAIR_VALUES = {1, 2, 4, 6, 8, 10, 12, 15, 18, 20};
    public static final int MAX_PAIR_LEVEL = 9;
    
    // Costos para mejorar valor de par
    public static final int[] PAIR_UPGRADE_COSTS = {0, 50, 150, 300, 500, 800, 1200, 1800, 2500, 4000};
    
    // ========== SISTEMA DE TIEMPO ==========
    // Tiempos base por nivel de mejora (en segundos)
    public static final int[] TIME_VALUES = {30, 40, 50, 60, 75, 90, 120};
    public static final int MAX_TIME_LEVEL = 6;
    
    // Costos para mejorar tiempo
    public static final int[] TIME_UPGRADE_COSTS = {0, 100, 250, 500, 1000, 2000, 5000};
    
    // Tiempo bonus por tipo de grid
    public static final int TIME_BONUS_4X4 = 0;
    public static final int TIME_BONUS_6X6 = 15;
    public static final int TIME_BONUS_8X8 = 30;
    
    // Tiempo extra por ver ad en derrota
    public static final int AD_EXTRA_TIME = 15;
    
    // ========== SISTEMA DE GALERÍA ==========
    public static final int TOTAL_CHARACTERS = 50;
    public static final int ARTS_PER_CHARACTER = 4; // Base + 3 mejoras (☆, ☆☆, ☆☆☆)
    
    // Costos de desbloqueo en galería
    public static final int GALLERY_BASE_COST = 250;
    public static final int GALLERY_STAR1_COST = 500;
    public static final int GALLERY_STAR2_COST = 750;
    public static final int GALLERY_STAR3_COST = 1000;
    
    // ========== SISTEMA DE NIVELES ==========
    public static final int TOTAL_LEVELS = 100;
    public static final int LEVELS_EASY_START = 1;
    public static final int LEVELS_EASY_END = 33;
    public static final int LEVELS_NORMAL_START = 34;
    public static final int LEVELS_NORMAL_END = 66;
    public static final int LEVELS_HARD_START = 67;
    public static final int LEVELS_HARD_END = 100;
    
    // Multiplicadores de dificultad
    public static final float MULTIPLIER_EASY = 1.0f;
    public static final float MULTIPLIER_NORMAL = 1.5f;
    public static final float MULTIPLIER_HARD = 2.0f;
    
    // ========== DAILY REWARD ==========
    public static final int[] DAILY_REWARDS = {10, 20, 30, 50, 100};
    public static final int DAILY_AD_BONUS = 50;
    
    // ========== MODIFICADORES DE JUEGO ==========
    public static final int SHUFFLE_INTERVAL = 4; // Cada 4 cartas volteadas
    
    // ========== PATHS DE ASSETS ==========
    public static final String PATH_IMAGES = "images/";
    public static final String PATH_CHARACTERS = "images/characters/";
    public static final String PATH_UI = "images/ui/";
    public static final String PATH_FRAMES = "images/frames/";
    public static final String PATH_AUDIO = "audio/";
    public static final String PATH_MUSIC = "audio/music/";
    public static final String PATH_SFX = "audio/sfx/";
    public static final String PATH_FONTS = "fonts/";
    public static final String PATH_DATA = "data/";
    
    // ========== NOMBRES DE ARCHIVOS ==========
    public static final String ATLAS_UI = "ui.atlas";
    public static final String ATLAS_CARDS = "cards.atlas";
    public static final String SKIN_DEFAULT = "skin/default.json";
    public static final String SAVE_FILE = "save.json";
    
    // ========== FRAMES/MARCOS ==========
    public static final String FRAME_BASE = "frame_base.png";
    public static final String FRAME_STAR1 = "frame_star1.png";
    public static final String FRAME_STAR2 = "frame_star2.png";
    public static final String FRAME_STAR3 = "frame_star3.png";
    
    // ========== AUDIO ==========
    public static final float DEFAULT_MUSIC_VOLUME = 0.7f;
    public static final float DEFAULT_SFX_VOLUME = 1.0f;
    
    // ========== ANIMACIONES ==========
    public static final float TRANSITION_DURATION = 0.3f;
    public static final float POPUP_ANIMATION_TIME = 0.25f;
    public static final float BUTTON_SCALE_PRESSED = 0.95f;
    
    // ========== COLORES (RGBA en float) ==========
    public static final float[] COLOR_PRIMARY = {0.94f, 0.36f, 0.58f, 1f};    // Rosa
    public static final float[] COLOR_SECONDARY = {0.53f, 0.26f, 0.78f, 1f};  // Púrpura
    public static final float[] COLOR_ACCENT = {1f, 0.84f, 0f, 1f};           // Dorado
    public static final float[] COLOR_BACKGROUND = {0.12f, 0.11f, 0.19f, 1f}; // Oscuro
    public static final float[] COLOR_TEXT = {1f, 1f, 1f, 1f};                // Blanco
    
    // ========== ADS ==========
    public static final int INTERSTITIAL_FREQUENCY = 5; // Cada 5 partidas
}