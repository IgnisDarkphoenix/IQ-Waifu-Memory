package com.waifu.memory.utils;

/**
 * Constantes globales del juego - VERSIÓN FINAL
 * ⚠️ NO MODIFICAR rutas ni nombres después de generar assets
 */
public final class Constants {
    
    private Constants() {} // Prevenir instanciación
    
    // ========== INFO DEL JUEGO ==========
    public static final String TAG = "IQWaifuMemory";
    public static final String VERSION = "1.0.0";
    public static final String GAME_TITLE = "IQ Waifu Memory";
    
    // ========== DIMENSIONES DE PANTALLA ==========
    public static final float WORLD_WIDTH = 1080f;
    public static final float WORLD_HEIGHT = 1920f;
    public static final float ASPECT_RATIO = WORLD_WIDTH / WORLD_HEIGHT;
    
    // ========== DIMENSIONES DE CARTAS ==========
    public static final float CARD_SIZE = 200f;
    public static final float CARD_PADDING = 10f;
    public static final float CARD_FLIP_TIME = 0.3f;
    public static final float CARD_SHOW_TIME = 0.8f;
    
    // ========== TAMAÑOS DE GRID ==========
    public static final int GRID_EASY = 4;    // 4x4 = 8 pares
    public static final int GRID_NORMAL = 6;  // 6x6 = 18 pares
    public static final int GRID_HARD = 8;    // 8x8 = 32 pares
    
    // ========== SISTEMA DE PCOINS (CONGELADO) ==========
    public static final String CURRENCY_NAME = "PCOINS";
    public static final int INITIAL_PCOINS = 0;
    
    // Valores de par por nivel de mejora
    public static final int[] PAIR_VALUES = {1, 2, 4, 6, 8, 10, 12, 15, 18, 20};
    public static final int MAX_PAIR_LEVEL = 9;
    public static final int[] PAIR_UPGRADE_COSTS = {0, 50, 150, 300, 500, 800, 1200, 1800, 2500, 4000};
    
    // ========== SISTEMA DE TIEMPO (CONGELADO) ==========
    public static final int[] TIME_VALUES = {30, 40, 50, 60, 75, 90, 120};
    public static final int MAX_TIME_LEVEL = 6;
    public static final int[] TIME_UPGRADE_COSTS = {0, 100, 250, 500, 1000, 2000, 5000};
    
    public static final int TIME_BONUS_4X4 = 0;
    public static final int TIME_BONUS_6X6 = 15;
    public static final int TIME_BONUS_8X8 = 30;
    public static final int AD_EXTRA_TIME = 15;
    
    // ========== SISTEMA DE GALERÍA (CONGELADO) ==========
    public static final int TOTAL_CHARACTERS = 50;
    public static final int VARIANTS_PER_CHARACTER = 4; // 0=Base, 1=☆, 2=☆☆, 3=☆☆☆
    
    // Costos de desbloqueo por variante (FINAL)
    public static final int GALLERY_COST_BASE = 250;    // Variante 0 (Base)
    public static final int GALLERY_COST_STAR1 = 500;   // Variante 1 (☆)
    public static final int GALLERY_COST_STAR2 = 750;   // Variante 2 (☆☆)
    public static final int GALLERY_COST_STAR3 = 1000;  // Variante 3 (☆☆☆)
    
    // Array para acceso programático
    public static final int[] GALLERY_COSTS = {
        GALLERY_COST_BASE,
        GALLERY_COST_STAR1,
        GALLERY_COST_STAR2,
        GALLERY_COST_STAR3
    };
    
    // ========== SISTEMA DE NIVELES (CONGELADO) ==========
    public static final int TOTAL_LEVELS = 100;
    public static final int LEVELS_EASY_START = 1;
    public static final int LEVELS_EASY_END = 33;
    public static final int LEVELS_NORMAL_START = 34;
    public static final int LEVELS_NORMAL_END = 66;
    public static final int LEVELS_HARD_START = 67;
    public static final int LEVELS_HARD_END = 100;
    
    public static final float MULTIPLIER_EASY = 1.0f;
    public static final float MULTIPLIER_NORMAL = 1.5f;
    public static final float MULTIPLIER_HARD = 2.0f;
    
    // ========== DAILY REWARD ==========
    public static final int[] DAILY_REWARDS = {10, 20, 30, 50, 100};
    public static final int DAILY_AD_BONUS = 50;
    
    // ========== MODIFICADORES ==========
    public static final int SHUFFLE_INTERVAL = 4;
    
    // ══════════════════════════════════════════════════════════
    // ⚠️ RUTAS DE ASSETS - CONGELADAS - NO MODIFICAR
    // ══════════════════════════════════════════════════════════
    
    // Rutas base
    public static final String PATH_IMAGES = "images/";
    public static final String PATH_AUDIO = "audio/";
    public static final String PATH_FONTS = "fonts/";
    public static final String PATH_DATA = "data/";
    
    // Rutas de imágenes
    public static final String PATH_CHARACTERS = "images/characters/";
    public static final String PATH_FRAMES = "images/frames/";
    public static final String PATH_UI = "images/ui/";
    
    // Rutas de audio
    public static final String PATH_MUSIC = "audio/music/";
    public static final String PATH_SFX = "audio/sfx/";
    
    // ══════════════════════════════════════════════════════════
    // ⚠️ NOMBRES DE ARCHIVOS - CONGELADOS - NO MODIFICAR
    // ══════════════════════════════════════════════════════════
    
    // Formato de nombres de personajes: char_XX_V.png
    // XX = ID del personaje (00-49)
    // V = Variante (0=Base, 1=☆, 2=☆☆, 3=☆☆☆)
    // Ejemplo: char_00_0.png, char_00_1.png, char_49_3.png
    public static final String CHARACTER_PREFIX = "char_";
    public static final String IMAGE_EXTENSION = ".png";
    
    // Marcos
    public static final String FRAME_BASE = "frame_base.png";
    public static final String FRAME_STAR1 = "frame_star1.png";
    public static final String FRAME_STAR2 = "frame_star2.png";
    public static final String FRAME_STAR3 = "frame_star3.png";
    
    // UI
    public static final String UI_LOGO = "logo.png";
    public static final String UI_CARD_BACK = "card_back.png";
    
    // Audio - SFX
    public static final String SFX_CARD_FLIP = "card_flip.ogg";
    public static final String SFX_MATCH = "match.ogg";
    public static final String SFX_NO_MATCH = "no_match.ogg";
    public static final String SFX_VICTORY = "victory.ogg";
    public static final String SFX_DEFEAT = "defeat.ogg";
    public static final String SFX_BUTTON = "button_click.ogg";
    public static final String SFX_COIN = "coin_collect.ogg";
    public static final String SFX_TIMER = "timer_warning.ogg";
    public static final String SFX_UNLOCK = "unlock.ogg";
    public static final String SFX_SHUFFLE = "shuffle.ogg";
    
    // Audio - Música
    public static final String MUSIC_MENU = "menu_theme.ogg";
    public static final String MUSIC_GAMEPLAY = "gameplay_theme.ogg";
    
    // ══════════════════════════════════════════════════════════
    // DIMENSIONES DE ASSETS (REFERENCIA PARA GENERACIÓN)
    // ══════════════════════════════════════════════════════════
    
    // Todas las imágenes en formato PNG
    public static final int ASSET_SIZE_CHARACTERS = 512;  // 512x512 px
    public static final int ASSET_SIZE_FRAMES = 512;      // 512x512 px
    public static final int ASSET_SIZE_CARD_BACK = 512;   // 512x512 px
    public static final int ASSET_SIZE_LOGO = 512;        // 512x512 px
    
    // ========== CONFIGURACIÓN UI ==========
    public static final float DEFAULT_MUSIC_VOLUME = 0.7f;
    public static final float DEFAULT_SFX_VOLUME = 1.0f;
    
    public static final float TRANSITION_DURATION = 0.3f;
    public static final float POPUP_ANIMATION_TIME = 0.25f;
    public static final float BUTTON_SCALE_PRESSED = 0.95f;
    
    // ========== COLORES ==========
    public static final float[] COLOR_PRIMARY = {0.94f, 0.36f, 0.58f, 1f};
    public static final float[] COLOR_SECONDARY = {0.53f, 0.26f, 0.78f, 1f};
    public static final float[] COLOR_ACCENT = {1f, 0.84f, 0f, 1f};
    public static final float[] COLOR_BACKGROUND = {0.12f, 0.11f, 0.19f, 1f};
    public static final float[] COLOR_TEXT = {1f, 1f, 1f, 1f};
    
    // Colores de rareza para UI
    public static final float[] COLOR_RARITY_BASE = {0.5f, 0.5f, 0.5f, 1f};    // Gris
    public static final float[] COLOR_RARITY_STAR1 = {0.8f, 0.5f, 0.2f, 1f};   // Bronce
    public static final float[] COLOR_RARITY_STAR2 = {0.75f, 0.75f, 0.8f, 1f}; // Plata
    public static final float[] COLOR_RARITY_STAR3 = {1f, 0.84f, 0f, 1f};      // Oro
    
    // ========== ADS ==========
    public static final int INTERSTITIAL_FREQUENCY = 5;
    
    // ══════════════════════════════════════════════════════════
    // MÉTODOS UTILITARIOS PARA NOMBRES DE ARCHIVOS
    // ══════════════════════════════════════════════════════════
    
    /**
     * Genera el nombre de archivo para un personaje
     * @param characterId ID del personaje (0-49)
     * @param variant Variante (0-3)
     * @return Nombre del archivo sin ruta (ej: "char_05_2.png")
     */
    public static String getCharacterFileName(int characterId, int variant) {
        return String.format("%s%02d_%d%s", 
            CHARACTER_PREFIX, characterId, variant, IMAGE_EXTENSION);
    }
    
    /**
     * Genera la ruta completa para un personaje
     * @param characterId ID del personaje (0-49)
     * @param variant Variante (0-3)
     * @return Ruta completa (ej: "images/characters/char_05_2.png")
     */
    public static String getCharacterPath(int characterId, int variant) {
        return PATH_CHARACTERS + getCharacterFileName(characterId, variant);
    }
    
    /**
     * Genera el nombre de archivo del marco según variante
     * @param variant Variante (0-3)
     * @return Nombre del archivo de marco
     */
    public static String getFrameFileName(int variant) {
        switch (variant) {
            case 0: return FRAME_BASE;
            case 1: return FRAME_STAR1;
            case 2: return FRAME_STAR2;
            case 3: return FRAME_STAR3;
            default: return FRAME_BASE;
        }
    }
    
    /**
     * Genera la ruta completa del marco
     */
    public static String getFramePath(int variant) {
        return PATH_FRAMES + getFrameFileName(variant);
    }
    
    /**
     * Obtiene el costo de desbloqueo para una variante
     * @param variant Variante a desbloquear (0-3)
     * @return Costo en PCOINS
     */
    public static int getGalleryCost(int variant) {
        if (variant < 0 || variant >= GALLERY_COSTS.length) {
            return -1;
        }
        return GALLERY_COSTS[variant];
    }
    
    /**
     * Obtiene el nombre de la variante para UI
     */
    public static String getVariantDisplayName(int variant) {
        switch (variant) {
            case 0: return "Base";
            case 1: return "★";
            case 2: return "★★";
            case 3: return "★★★";
            default: return "???";
        }
    }
    
    /**
     * Obtiene el color de rareza
     */
    public static float[] getRarityColor(int variant) {
        switch (variant) {
            case 0: return COLOR_RARITY_BASE;
            case 1: return COLOR_RARITY_STAR1;
            case 2: return COLOR_RARITY_STAR2;
            case 3: return COLOR_RARITY_STAR3;
            default: return COLOR_RARITY_BASE;
        }
    }
}