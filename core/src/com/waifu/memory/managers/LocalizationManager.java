package com.waifu.memory.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Sistema de localización multi-idioma para IQ Waifu Memory
 * Soporta 15 idiomas principales del público anime/waifu
 */
public class LocalizationManager {
    
    private static final String PREFS_NAME = "IQWaifuMemory_Settings";
    private static final String PREF_LANGUAGE = "selected_language";
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String LOCALIZATION_PATH = "data/localization/";
    
    public enum Language {
        ENGLISH("en", "English", "🇺🇸"),
        SPANISH("es", "Español", "🇪🇸"),
        JAPANESE("ja", "日本語", "🇯🇵"),
        CHINESE_SIMPLIFIED("zh", "简体中文", "🇨🇳"),
        KOREAN("ko", "한국어", "🇰🇷"),
        FRENCH("fr", "Français", "🇫🇷"),
        GERMAN("de", "Deutsch", "🇩🇪"),
        PORTUGUESE("pt", "Português", "🇵🇹"),
        RUSSIAN("ru", "Русский", "🇷🇺"),
        ITALIAN("it", "Italiano", "🇮🇹"),
        THAI("th", "ไทย", "🇹🇭"),
        VIETNAMESE("vi", "Tiếng Việt", "🇻🇳"),
        INDONESIAN("id", "Bahasa Indonesia", "🇮🇩"),
        FILIPINO("fil", "Tagalog", "🇵🇭"),
        TURKISH("tr", "Türkçe", "🇹🇷");
        
        public final String code;
        public final String displayName;
        public final String flag;
        
        Language(String code, String displayName, String flag) {
            this.code = code;
            this.displayName = displayName;
            this.flag = flag;
        }
        
        public static Language fromCode(String code) {
            for (Language lang : values()) {
                if (lang.code.equals(code)) return lang;
            }
            return ENGLISH;
        }
    }
    
    private Language currentLanguage;
    private final ObjectMap<String, String> strings;
    private final Preferences prefs;
    
    public LocalizationManager() {
        this.strings = new ObjectMap<>();
        this.prefs = Gdx.app.getPreferences(PREFS_NAME);
        
        // Detectar idioma del sistema o cargar guardado
        String savedLang = prefs.getString(PREF_LANGUAGE, null);
        if (savedLang != null) {
            currentLanguage = Language.fromCode(savedLang);
        } else {
            currentLanguage = detectSystemLanguage();
        }
        
        loadLanguage(currentLanguage);
    }
    
    /**
     * Detecta el idioma del sistema operativo
     */
    private Language detectSystemLanguage() {
        String sysLang = System.getProperty("user.language", "en").toLowerCase();
        
        // Mapeo de códigos ISO a nuestros idiomas
        switch (sysLang) {
            case "es": return Language.SPANISH;
            case "ja": return Language.JAPANESE;
            case "zh": return Language.CHINESE_SIMPLIFIED;
            case "ko": return Language.KOREAN;
            case "fr": return Language.FRENCH;
            case "de": return Language.GERMAN;
            case "pt": return Language.PORTUGUESE;
            case "ru": return Language.RUSSIAN;
            case "it": return Language.ITALIAN;
            case "th": return Language.THAI;
            case "vi": return Language.VIETNAMESE;
            case "id": return Language.INDONESIAN;
            case "tl": case "fil": return Language.FILIPINO;
            case "tr": return Language.TURKISH;
            default: return Language.ENGLISH;
        }
    }
    
    /**
     * Carga el archivo de idioma desde JSON
     */
    private void loadLanguage(Language language) {
        strings.clear();
        
        String filePath = LOCALIZATION_PATH + language.code + ".json";
        
        try {
            if (!Gdx.files.internal(filePath).exists()) {
                Gdx.app.log("Localization", "File not found: " + filePath + ", using fallback");
                loadFallbackStrings();
                return;
            }
            
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(Gdx.files.internal(filePath));
            
            for (JsonValue entry = root.child; entry != null; entry = entry.next) {
                strings.put(entry.name, entry.asString());
            }
            
            Gdx.app.log("Localization", "Loaded language: " + language.displayName);
            
        } catch (Exception e) {
            Gdx.app.error("Localization", "Error loading " + filePath, e);
            loadFallbackStrings();
        }
    }
    
    /**
     * Carga strings en inglés como fallback
     */
    private void loadFallbackStrings() {
        // UI General
        strings.put("game_title", "IQ Waifu Memory");
        strings.put("play", "PLAY");
        strings.put("upgrades", "UPGRADES");
        strings.put("gallery", "GALLERY");
        strings.put("settings", "SETTINGS");
        strings.put("back", "BACK");
        strings.put("continue", "CONTINUE");
        strings.put("restart", "RESTART");
        strings.put("exit", "EXIT");
        strings.put("home", "HOME");
        strings.put("next", "NEXT");
        strings.put("confirm", "CONFIRM");
        strings.put("cancel", "CANCEL");
        
        // Gameplay
        strings.put("level", "LEVEL");
        strings.put("pairs", "Pairs");
        strings.put("time", "Time");
        strings.put("victory", "VICTORY!");
        strings.put("defeat", "TIME'S UP");
        strings.put("paused", "PAUSED");
        strings.put("hint", "HINT");
        
        // Economy
        strings.put("pcoins", "PCOINS");
        strings.put("earned", "Earned");
        strings.put("cost", "Cost");
        strings.put("unlock", "UNLOCK");
        strings.put("locked", "LOCKED");
        strings.put("owned", "OWNED");
        
        // Upgrades
        strings.put("upgrade_pair_value", "Pair Value");
        strings.put("upgrade_time", "Extra Time");
        strings.put("upgrade_max", "MAX LEVEL");
        
        // Ads
        strings.put("ad_double", "x2 AD");
        strings.put("ad_extra_time", "+15s AD");
        strings.put("ad_hint", "HINT AD");
        strings.put("watch_ad", "WATCH AD");
        
        // Settings
        strings.put("music", "Music");
        strings.put("sfx", "Sound Effects");
        strings.put("language", "Language");
        strings.put("credits", "Credits");
        
        // Gallery
        strings.put("character", "Character");
        strings.put("variant", "Variant");
        strings.put("base", "Base");
        strings.put("star1", "★");
        strings.put("star2", "★★");
        strings.put("star3", "★★★");
        
        // Messages
        strings.put("msg_not_enough_pcoins", "Not enough PCOINS!");
        strings.put("msg_level_locked", "Complete previous level first");
        strings.put("msg_ad_not_available", "Ad not available");
        strings.put("msg_unlocked", "Unlocked!");
    }
    
    /**
     * Cambia el idioma activo
     */
    public void setLanguage(Language language) {
        if (language == currentLanguage) return;
        
        currentLanguage = language;
        loadLanguage(language);
        
        // Guardar preferencia
        prefs.putString(PREF_LANGUAGE, language.code);
        prefs.flush();
    }
    
    /**
     * Obtiene el idioma actual
     */
    public Language getCurrentLanguage() {
        return currentLanguage;
    }
    
    /**
     * Obtiene todas las opciones de idioma
     */
    public Language[] getAvailableLanguages() {
        return Language.values();
    }
    
    /**
     * Obtiene un string traducido por su clave
     * Si no existe, devuelve la clave misma para debugging
     */
    public String get(String key) {
        if (strings.containsKey(key)) {
            return strings.get(key);
        }
        Gdx.app.log("Localization", "Missing key: " + key);
        return "[" + key + "]";
    }
    
    /**
     * Obtiene un string con formato (reemplazo de variables)
     * Ejemplo: get("level_x", "5") -> "Level 5"
     */
    public String get(String key, Object... args) {
        String template = get(key);
        return String.format(template, args);
    }
    
    /**
     * Obtiene string con sustitución de marcadores
     * Ejemplo: get("pairs_found", "{current}", 3, "{total}", 8) -> "Pairs: 3/8"
     */
    public String getWithPlaceholders(String key, Object... replacements) {
        String text = get(key);
        
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                String placeholder = replacements[i].toString();
                String value = replacements[i + 1].toString();
                text = text.replace(placeholder, value);
            }
        }
        
        return text;
    }
    
    /**
     * Helper para obtener nombre de nivel formateado
     */
    public String getLevelText(int levelNumber) {
        return get("level") + " " + levelNumber;
    }
    
    /**
     * Helper para obtener texto de pares
     */
    public String getPairsText(int current, int total) {
        return get("pairs") + ": " + current + "/" + total;
    }
    
    /**
     * Helper para obtener texto de monedas
     */
    public String getPcoinsText(int amount) {
        return amount + " " + get("pcoins");
    }
    
    /**
     * Helper para tiempo formateado
     */
    public String getTimeText(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }
}