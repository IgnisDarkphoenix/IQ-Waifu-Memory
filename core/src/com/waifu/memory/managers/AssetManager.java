package com.waifu.memory.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.waifu.memory.utils.Constants;

/**
 * Manager para cargar y gestionar assets gráficos
 * Implementa lazy loading y fallbacks visuales
 */
public class AssetManager implements Disposable {
    
    // Cache de texturas de personajes (lazy loading)
    private ObjectMap<String, Texture> characterTextures;
    
    // Texturas esenciales (siempre cargadas)
    private Texture cardBackTexture;
    private Texture logoTexture;
    
    // Marcos por rareza
    private Texture[] frameTextures;
    
    // Texturas placeholder (generadas dinámicamente)
    private Texture placeholderCharacter;
    private Texture placeholderCardBack;
    private Texture[] placeholderFrames;
    
    // Estado
    private boolean essentialsLoaded;
    
    public AssetManager() {
        characterTextures = new ObjectMap<>();
        frameTextures = new Texture[Constants.VARIANTS_PER_CHARACTER];
        placeholderFrames = new Texture[Constants.VARIANTS_PER_CHARACTER];
        essentialsLoaded = false;
    }
    
    /**
     * Carga los assets esenciales
     * Llamar en SplashScreen
     */
    public void loadEssentialAssets() {
        Gdx.app.log(Constants.TAG, "Cargando assets esenciales...");
        
        // Generar placeholders primero (siempre disponibles)
        generatePlaceholders();
        
        // Intentar cargar assets reales
        loadRealAssets();
        
        essentialsLoaded = true;
        Gdx.app.log(Constants.TAG, "Assets esenciales listos");
    }
    
    /**
     * Genera texturas placeholder para cuando no existan los assets reales
     */
    private void generatePlaceholders() {
        int size = Constants.ASSET_SIZE_CHARACTERS;
        
        // Placeholder para personajes (gris con patrón)
        placeholderCharacter = createPlaceholderTexture(size, 
            new Color(0.3f, 0.3f, 0.4f, 1f), "?");
        
        // Placeholder para card back (azul oscuro)
        placeholderCardBack = createPlaceholderTexture(size,
            new Color(0.15f, 0.15f, 0.3f, 1f), "CARD");
        
        // Placeholders para marcos (colores por rareza)
        Color[] frameColors = {
            new Color(0.4f, 0.4f, 0.45f, 1f),  // Base - Gris
            new Color(0.7f, 0.45f, 0.2f, 1f),  // ☆ - Bronce
            new Color(0.7f, 0.7f, 0.75f, 1f),  // ☆☆ - Plata
            new Color(0.9f, 0.75f, 0.1f, 1f)   // ☆☆☆ - Oro
        };
        
        for (int i = 0; i < Constants.VARIANTS_PER_CHARACTER; i++) {
            placeholderFrames[i] = createFramePlaceholder(size, frameColors[i], i);
        }
        
        Gdx.app.log(Constants.TAG, "Placeholders generados");
    }
    
    /**
     * Crea una textura placeholder simple
     */
    private Texture createPlaceholderTexture(int size, Color color, String label) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        
        // Fondo
        pixmap.setColor(color);
        pixmap.fill();
        
        // Borde
        pixmap.setColor(Color.WHITE);
        pixmap.drawRectangle(0, 0, size, size);
        pixmap.drawRectangle(1, 1, size - 2, size - 2);
        
        // Patrón diagonal
        pixmap.setColor(new Color(1f, 1f, 1f, 0.1f));
        for (int i = 0; i < size * 2; i += 20) {
            pixmap.drawLine(i, 0, 0, i);
        }
        
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        
        return texture;
    }
    
    /**
     * Crea un placeholder de marco con borde decorativo
     */
    private Texture createFramePlaceholder(int size, Color color, int variant) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        
        // Transparente en el centro
        pixmap.setColor(Color.CLEAR);
        pixmap.fill();
        
        int borderWidth = 20;
        
        // Borde exterior
        pixmap.setColor(color);
        
        // Top
        pixmap.fillRectangle(0, 0, size, borderWidth);
        // Bottom
        pixmap.fillRectangle(0, size - borderWidth, size, borderWidth);
        // Left
        pixmap.fillRectangle(0, 0, borderWidth, size);
        // Right
        pixmap.fillRectangle(size - borderWidth, 0, borderWidth, size);
        
        // Esquinas decorativas
        pixmap.setColor(Color.WHITE);
        int cornerSize = 30;
        // Top-left
        pixmap.fillRectangle(0, 0, cornerSize, 5);
        pixmap.fillRectangle(0, 0, 5, cornerSize);
        // Top-right
        pixmap.fillRectangle(size - cornerSize, 0, cornerSize, 5);
        pixmap.fillRectangle(size - 5, 0, 5, cornerSize);
        // Bottom-left
        pixmap.fillRectangle(0, size - 5, cornerSize, 5);
        pixmap.fillRectangle(0, size - cornerSize, 5, cornerSize);
        // Bottom-right
        pixmap.fillRectangle(size - cornerSize, size - 5, cornerSize, 5);
        pixmap.fillRectangle(size - 5, size - cornerSize, 5, cornerSize);
        
        // Indicador de estrellas en la parte inferior
        if (variant > 0) {
            int starY = size - borderWidth + 5;
            int starSize = 10;
            int totalWidth = variant * (starSize + 5);
            int startX = (size - totalWidth) / 2;
            
            pixmap.setColor(Color.WHITE);
            for (int s = 0; s < variant; s++) {
                int x = startX + s * (starSize + 5);
                pixmap.fillRectangle(x, starY, starSize, starSize);
            }
        }
        
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        
        return texture;
    }
    
    /**
     * Intenta cargar los assets reales desde archivos
     */
    private void loadRealAssets() {
        // Card back
        String cardBackPath = Constants.PATH_UI + Constants.UI_CARD_BACK;
        if (fileExists(cardBackPath)) {
            cardBackTexture = loadTexture(cardBackPath);
            Gdx.app.log(Constants.TAG, "Card back cargado");
        } else {
            Gdx.app.log(Constants.TAG, "Card back no encontrado, usando placeholder");
        }
        
        // Logo
        String logoPath = Constants.PATH_UI + Constants.UI_LOGO;
        if (fileExists(logoPath)) {
            logoTexture = loadTexture(logoPath);
            Gdx.app.log(Constants.TAG, "Logo cargado");
        }
        
        // Marcos
        for (int i = 0; i < Constants.VARIANTS_PER_CHARACTER; i++) {
            String framePath = Constants.getFramePath(i);
            if (fileExists(framePath)) {
                frameTextures[i] = loadTexture(framePath);
                Gdx.app.log(Constants.TAG, "Marco " + i + " cargado");
            }
        }
    }
    
    /**
     * Carga una textura desde archivo
     */
    private Texture loadTexture(String path) {
        try {
            Texture texture = new Texture(Gdx.files.internal(path));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return texture;
        } catch (Exception e) {
            Gdx.app.error(Constants.TAG, "Error cargando: " + path + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Verifica si un archivo existe
     */
    private boolean fileExists(String path) {
        return Gdx.files.internal(path).exists();
    }
    
    // ══════════════════════════════════════════════════════════
    // GETTERS CON FALLBACK
    // ══════════════════════════════════════════════════════════
    
    /**
     * Obtiene la textura del reverso de carta (nunca null)
     */
    public Texture getCardBackTexture() {
        return cardBackTexture != null ? cardBackTexture : placeholderCardBack;
    }
    
    /**
     * Obtiene la textura del logo (puede ser null)
     */
    public Texture getLogoTexture() {
        return logoTexture;
    }
    
    /**
     * Obtiene el marco para una variante (nunca null)
     */
    public Texture getFrameTexture(int variant) {
        if (variant < 0 || variant >= Constants.VARIANTS_PER_CHARACTER) {
            variant = 0;
        }
        
        Texture real = frameTextures[variant];
        return real != null ? real : placeholderFrames[variant];
    }
    
    /**
     * Verifica si existe el marco real (no placeholder)
     */
    public boolean hasRealFrame(int variant) {
        if (variant < 0 || variant >= Constants.VARIANTS_PER_CHARACTER) {
            return false;
        }
        return frameTextures[variant] != null;
    }
    
    // ══════════════════════════════════════════════════════════
    // PERSONAJES - LAZY LOADING
    // ══════════════════════════════════════════════════════════
    
    /**
     * Obtiene textura de personaje con lazy loading (nunca null)
     */
    public Texture getCharacterTexture(int characterId, int variant) {
        // Validar
        if (characterId < 0 || characterId >= Constants.TOTAL_CHARACTERS ||
            variant < 0 || variant >= Constants.VARIANTS_PER_CHARACTER) {
            return placeholderCharacter;
        }
        
        String key = characterId + "_" + variant;
        
        // Verificar cache
        if (characterTextures.containsKey(key)) {
            Texture cached = characterTextures.get(key);
            return cached != null ? cached : placeholderCharacter;
        }
        
        // Intentar cargar
        String path = Constants.getCharacterPath(characterId, variant);
        
        if (fileExists(path)) {
            Texture texture = loadTexture(path);
            characterTextures.put(key, texture);
            
            if (texture != null) {
                Gdx.app.log(Constants.TAG, "Personaje cargado: " + key);
                return texture;
            }
        }
        
        // Marcar como intentado (null en cache = no existe)
        characterTextures.put(key, null);
        return placeholderCharacter;
    }
    
    /**
     * Verifica si existe la textura real de un personaje
     */
    public boolean hasRealCharacterTexture(int characterId, int variant) {
        String path = Constants.getCharacterPath(characterId, variant);
        return fileExists(path);
    }
    
    /**
     * Descarga una textura de personaje
     */
    public void unloadCharacterTexture(int characterId, int variant) {
        String key = characterId + "_" + variant;
        
        if (characterTextures.containsKey(key)) {
            Texture texture = characterTextures.get(key);
            if (texture != null) {
                texture.dispose();
            }
            characterTextures.remove(key);
        }
    }
    
    /**
     * Descarga todas las variantes de un personaje
     */
    public void unloadCharacter(int characterId) {
        for (int v = 0; v < Constants.VARIANTS_PER_CHARACTER; v++) {
            unloadCharacterTexture(characterId, v);
        }
    }
    
    /**
     * Descarga todos los personajes del cache
     */
    public void unloadAllCharacters() {
        for (Texture texture : characterTextures.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        characterTextures.clear();
        Gdx.app.log(Constants.TAG, "Todos los personajes descargados");
    }
    
    /**
     * Precarga personajes para un nivel
     */
    public void preloadCharactersForLevel(int[] characterIds) {
        for (int id : characterIds) {
            getCharacterTexture(id, 0); // Solo variante base para gameplay
        }
    }
    
    // ══════════════════════════════════════════════════════════
    // INFO Y DEBUG
    // ══════════════════════════════════════════════════════════
    
    public boolean isEssentialLoaded() {
        return essentialsLoaded;
    }
    
    public int getCachedCharacterCount() {
        return characterTextures.size;
    }
    
    public String getDebugInfo() {
        int realFrames = 0;
        for (Texture t : frameTextures) {
            if (t != null) realFrames++;
        }
        
        return String.format(
            "Assets: CardBack=%s, Logo=%s, Frames=%d/4, Characters=%d cached",
            cardBackTexture != null ? "OK" : "PLACEHOLDER",
            logoTexture != null ? "OK" : "NO",
            realFrames,
            characterTextures.size
        );
    }
    
    @Override
    public void dispose() {
        Gdx.app.log(Constants.TAG, "Disposing AssetManager...");
        
        // Texturas reales
        if (cardBackTexture != null) cardBackTexture.dispose();
        if (logoTexture != null) logoTexture.dispose();
        
        for (Texture t : frameTextures) {
            if (t != null) t.dispose();
        }
        
        // Placeholders
        if (placeholderCharacter != null) placeholderCharacter.dispose();
        if (placeholderCardBack != null) placeholderCardBack.dispose();
        
        for (Texture t : placeholderFrames) {
            if (t != null) t.dispose();
        }
        
        // Cache de personajes
        unloadAllCharacters();
        
        essentialsLoaded = false;
        Gdx.app.log(Constants.TAG, "AssetManager disposed");
    }
}