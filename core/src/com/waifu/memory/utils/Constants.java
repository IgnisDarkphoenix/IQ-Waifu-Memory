package com.waifu.memory.utils;

public final class Constants {

    private Constants() {}

    public static final String TAG = "IQWaifuMemory";
    public static final String VERSION = "1.0.0";
    public static final String GAME_TITLE = "IQ Waifu Memory";

    public static final float WORLD_WIDTH = 1080f;
    public static final float WORLD_HEIGHT = 1920f;
    public static final float ASPECT_RATIO = WORLD_WIDTH / WORLD_HEIGHT;

    public static final float CARD_SIZE = 200f;
    public static final float CARD_PADDING = 10f;
    public static final float CARD_FLIP_TIME = 0.3f;
    public static final float CARD_SHOW_TIME = 0.8f;

    public static final int GRID_EASY = 4;
    public static final int GRID_NORMAL = 6;
    public static final int GRID_HARD = 8;

    public static final String CURRENCY_NAME = "PCOINS";
    public static final int INITIAL_PCOINS = 0;

    public static final int[] PAIR_VALUES = {1, 2, 4, 6, 8, 10, 12, 15, 18, 20};
    public static final int MAX_PAIR_LEVEL = 9;
    public static final int[] PAIR_UPGRADE_COSTS = {0, 50, 150, 300, 500, 800, 1200, 1800, 2500, 4000};

    public static final int[] TIME_VALUES = {30, 40, 50, 60, 75, 90, 120};
    public static final int MAX_TIME_LEVEL = 6;
    public static final int[] TIME_UPGRADE_COSTS = {0, 100, 250, 500, 1000, 2000, 5000};

    public static final int TIME_BONUS_4X4 = 0;
    public static final int TIME_BONUS_6X6 = 15;
    public static final int TIME_BONUS_8X8 = 30;

    public static final int AD_EXTRA_TIME = 15;

    public static final int TOTAL_CHARACTERS = 50;
    public static final int VARIANTS_PER_CHARACTER = 4;
    public static final int ARTS_PER_CHARACTER = VARIANTS_PER_CHARACTER;

    public static final int GALLERY_COST_BASE = 250;
    public static final int GALLERY_COST_STAR1 = 500;
    public static final int GALLERY_COST_STAR2 = 750;
    public static final int GALLERY_COST_STAR3 = 1000;

    public static final int[] GALLERY_COSTS = {GALLERY_COST_BASE, GALLERY_COST_STAR1, GALLERY_COST_STAR2, GALLERY_COST_STAR3};

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

    public static final int[] DAILY_REWARDS = {10, 20, 30, 50, 100};
    public static final int DAILY_AD_BONUS = 50;

    public static final int SHUFFLE_INTERVAL = 4;

    public static final String PATH_IMAGES = "images/";
    public static final String PATH_AUDIO = "audio/";
    public static final String PATH_FONTS = "fonts/";
    public static final String PATH_DATA = "data/";

    public static final String PATH_CHARACTERS = "images/characters/";
    public static final String PATH_FRAMES = "images/frames/";
    public static final String PATH_UI = "images/ui/";

    public static final String PATH_MUSIC = "audio/music/";
    public static final String PATH_SFX = "audio/sfx/";

    public static final String CHARACTER_PREFIX = "char_";
    public static final String IMAGE_EXTENSION = ".png";

    public static final String FRAME_BASE = "frame_base.png";
    public static final String FRAME_STAR1 = "frame_star1.png";
    public static final String FRAME_STAR2 = "frame_star2.png";
    public static final String FRAME_STAR3 = "frame_star3.png";

    public static final String UI_LOGO = "logo.png";
    public static final String UI_CARD_BACK = "card_back.png";

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

    public static final String MUSIC_MENU = "menu_theme.ogg";
    public static final String MUSIC_GAMEPLAY = "gameplay_theme.ogg";

    public static final int ASSET_CARD_WIDTH = 512;
    public static final int ASSET_CARD_HEIGHT = 720;

    public static final int ASSET_HD_WIDTH = 1024;
    public static final int ASSET_HD_HEIGHT = 1440;

    public static final int ASSET_SIZE_LOGO = 512;

    public static final int ASSET_SIZE_CHARACTERS = ASSET_CARD_WIDTH;
    public static final int ASSET_SIZE_CHARACTERS_H = ASSET_CARD_HEIGHT;
    public static final int ASSET_SIZE_FRAMES = ASSET_CARD_WIDTH;
    public static final int ASSET_SIZE_FRAMES_H = ASSET_CARD_HEIGHT;
    public static final int ASSET_SIZE_CARD_BACK = ASSET_CARD_WIDTH;
    public static final int ASSET_SIZE_CARD_BACK_H = ASSET_CARD_HEIGHT;

    public static final float CARD_ASPECT_RATIO = (float) ASSET_CARD_WIDTH / (float) ASSET_CARD_HEIGHT;
    public static final float CARD_WORLD_WIDTH = CARD_SIZE;
    public static final float CARD_WORLD_HEIGHT = CARD_WORLD_WIDTH / CARD_ASPECT_RATIO;

    public static final float DEFAULT_MUSIC_VOLUME = 0.7f;
    public static final float DEFAULT_SFX_VOLUME = 1.0f;

    public static final float TRANSITION_DURATION = 0.3f;
    public static final float POPUP_ANIMATION_TIME = 0.25f;
    public static final float BUTTON_SCALE_PRESSED = 0.95f;

    public static final float[] COLOR_PRIMARY = {0.94f, 0.36f, 0.58f, 1f};
    public static final float[] COLOR_SECONDARY = {0.53f, 0.26f, 0.78f, 1f};
    public static final float[] COLOR_ACCENT = {1f, 0.84f, 0f, 1f};
    public static final float[] COLOR_BACKGROUND = {0.12f, 0.11f, 0.19f, 1f};
    public static final float[] COLOR_TEXT = {1f, 1f, 1f, 1f};

    public static final float[] COLOR_RARITY_BASE = {0.5f, 0.5f, 0.5f, 1f};
    public static final float[] COLOR_RARITY_STAR1 = {0.8f, 0.5f, 0.2f, 1f};
    public static final float[] COLOR_RARITY_STAR2 = {0.75f, 0.75f, 0.8f, 1f};
    public static final float[] COLOR_RARITY_STAR3 = {1f, 0.84f, 0f, 1f};

    public static final int INTERSTITIAL_FREQUENCY = 3;
    public static final int INTERSTITIAL_NEW_PLAYER_GRACE_GAMES = 9;
    public static final boolean INTERSTITIAL_ON_EXIT_ENABLED = true;

    public static final int HINTS_PER_MATCH = 3;
    public static final int HINT_MIN_GRID_SIZE = 6;
    public static final int HINT_SHAKE_TOTAL_CARDS = 4;
    public static final int HINT_SHAKE_PAIR_CARDS = 2;
    public static final int HINT_SHAKE_DECOY_CARDS = 2;

    public static final String AD_PLACEMENT_REWARDED_EXTRA_TIME = "REWARDED_EXTRA_TIME";
    public static final String AD_PLACEMENT_REWARDED_DOUBLE_COINS = "REWARDED_DOUBLE_COINS";
    public static final String AD_PLACEMENT_REWARDED_HINT = "REWARDED_HINT";
    public static final String AD_PLACEMENT_REWARDED_HD_DOWNLOAD = "REWARDED_HD_DOWNLOAD";
    public static final String AD_PLACEMENT_INTERSTITIAL = "INTERSTITIAL";
    public static final String AD_PLACEMENT_INTERSTITIAL_EXIT = "INTERSTITIAL_EXIT";

    public static final String LEVELS_JSON = "levels.json";
    public static final String LEVELS_JSON_PATH = PATH_DATA + LEVELS_JSON;

    public static final String HD_LINKS_JSON = "hd_links.json";
    public static final String HD_LINKS_JSON_PATH = PATH_DATA + HD_LINKS_JSON;

    public static final int HD_DOWNLOAD_VARIANT = 3;

    public static String getCharacterFileName(int characterId, int variant) {
        return String.format("%s%02d_%d%s", CHARACTER_PREFIX, characterId, variant, IMAGE_EXTENSION);
    }

    public static String getCharacterPath(int characterId, int variant) {
        return PATH_CHARACTERS + getCharacterFileName(characterId, variant);
    }

    public static String getFrameFileName(int variant) {
        switch (variant) {
            case 0: return FRAME_BASE;
            case 1: return FRAME_STAR1;
            case 2: return FRAME_STAR2;
            case 3: return FRAME_STAR3;
            default: return FRAME_BASE;
        }
    }

    public static String getFramePath(int variant) {
        return PATH_FRAMES + getFrameFileName(variant);
    }

    public static String getUiPath(String fileName) {
        return PATH_UI + fileName;
    }

    public static String getSfxPath(String fileName) {
        return PATH_SFX + fileName;
    }

    public static String getMusicPath(String fileName) {
        return PATH_MUSIC + fileName;
    }

    public static int getGalleryCost(int variant) {
        if (variant < 0 || variant >= GALLERY_COSTS.length) return -1;
        return GALLERY_COSTS[variant];
    }

    public static String getVariantDisplayName(int variant) {
        switch (variant) {
            case 0: return "Base";
            case 1: return "★";
            case 2: return "★★";
            case 3: return "★★★";
            default: return "???";
        }
    }

    public static float[] getRarityColor(int variant) {
        switch (variant) {
            case 0: return COLOR_RARITY_BASE;
            case 1: return COLOR_RARITY_STAR1;
            case 2: return COLOR_RARITY_STAR2;
            case 3: return COLOR_RARITY_STAR3;
            default: return COLOR_RARITY_BASE;
        }
    }

    public static boolean isValidCharacterId(int characterId) {
        return characterId >= 0 && characterId < TOTAL_CHARACTERS;
    }

    public static boolean isValidVariant(int variant) {
        return variant >= 0 && variant < VARIANTS_PER_CHARACTER;
    }

    public static boolean isValidLevel(int level) {
        return level >= 1 && level <= TOTAL_LEVELS;
    }

    public static boolean isHintsEnabledForGrid(int gridSize) {
        return gridSize >= HINT_MIN_GRID_SIZE;
    }

    public static boolean isHintShakeConfigValid() {
        return HINT_SHAKE_TOTAL_CARDS == (HINT_SHAKE_PAIR_CARDS + HINT_SHAKE_DECOY_CARDS);
    }

    public static boolean isInterstitialEligible(int totalGamesPlayed) {
        return totalGamesPlayed >= INTERSTITIAL_NEW_PLAYER_GRACE_GAMES;
    }
}