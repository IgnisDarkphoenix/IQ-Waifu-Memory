package com.waifu.memory.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.waifu.memory.utils.Constants;

/**
 * Manager para cargar y gestionar assets gráficos
 * Implementa lazy loading para optimizar memoria
 */
public class AssetManager implements Disposable {
    
    // Atlas de UI (siempre cargado)
    private TextureAtlas uiAtlas;
    
    // Cache de texturas de personajes (lazy loading)
    private ObjectMap<String, Texture> characterTextures;
    
    // Texturas de cartas base
    private Texture cardBackTexture;
    
    // Marcos por rareza (Base, ☆, ☆☆, ☆☆☆)
    private Texture[] frameTextures;
    private static final int FRAME_COUNT = 4;
    
    // Estado de carga
    private boolean uiLoaded;
    private boolean framesLoaded;
    
    public AssetManager() {
        characterTextures = new ObjectMap<>();
        frameTextures = new Texture[FRAME_COUNT];
        uiLoaded = false;
        framesLoaded = false;
    }
    
    /**
     * Carga los assets esenciales de UI
     * Llamar en SplashScreen
     */
    public void loadEssentialAssets() {
        Gdx.app.log(Constants.TAG, "Cargando assets esenciales...");
        
        try {
            // Cargar atlas de UI si existe
            String atlasPath = Constants.PATH_UI + Constants.ATLAS_UI;
            if (Gdx.files.internal(atlasPath).exists()) {
                uiAtlas = new TextureAtlas(Gdx.files.internal(atlasPath));
                Gdx.app.log(Constants.TAG, "Atlas UI cargado");
            } else {
                Gdx.app.log(Constants.TAG, "Atlas UI no encontrado: " + atlasPath);
            }
            
            // Cargar textura del reverso de carta
            String cardBackPath = Constants.PATH_UI + "card_back.png";
            if (Gdx.files.internal(cardBackPath).exists()) {
                cardBackTexture = new Texture(Gdx.files.internal(cardBackPath));
                cardBackTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                Gdx.app.log(Constants.TAG, "Card back cargado");
            } else {
                Gdx.app.log(Constants.TAG, "Card back no encontrado: " + cardBackPath);
            }
            
            // Cargar marcos de rareza
            loadFrameTextures();
            
            uiLoaded = true;
            Gdx.app.log(Constants.TAG, "Assets esenciales cargados correctamente");
            
        } catch (Exception e) {
            Gdx.app.error(Constants.TAG, "Error cargando assets esenciales: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Carga las texturas de marcos por rareza
     */
    private void loadFrameTextures() {
        String[] frameFiles = {
            Constants.FRAME_BASE,   // 0 = Base
            Constants.FRAME_STAR1,  // 1 = ☆
            Constants.FRAME_STAR2,  // 2 = ☆☆
            Constants.FRAME_STAR3   // 3 = ☆☆☆
        };
        
        int loadedCount = 0;
        
        for (int i = 0; i < frameFiles.length; i++) {
            String path = Constants.PATH_FRAMES + frameFiles[i];
            
            if (Gdx.files.internal(path).exists()) {
                try {
                    frameTextures[i] = new Texture(Gdx.files.internal(path));
                    frameTextures[i].setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                    loadedCount++;
                    Gdx.app.log(Constants.TAG, "Marco cargado: " + frameFiles[i]);
                } catch (Exception e) {
                    Gdx.app.error(Constants.TAG, "Error cargando marco: " + path + " - " + e.getMessage());
                    frameTextures[i] = null;
                }
            } else {
                Gdx.app.log(Constants.TAG, "Marco no encontrado (opcional): " + path);
                frameTextures[i] = null;
            }
        }
        
        framesLoaded = true;
        Gdx.app.log(Constants.TAG, "Marcos cargados: " + loadedCount + "/" + frameFiles.length);
    }
    
    /**
     * Obtiene el marco para una rareza específica
     * @param rarity 0=base, 1=☆, 2=☆☆, 3=☆☆☆
     * @return Textura del marco o null si no existe
     */
    public Texture getFrameTexture(int rarity) {
        if (rarity < 0 || rarity >= FRAME_COUNT) {
            return null;
        }
        return frameTextures[rarity];
    }
    
    /**
     * Verifica si hay al menos un marco cargado
     */
    public boolean hasAnyFrameLoaded() {
        for (Texture frame : frameTextures) {
            if (frame != null) return true;
        }
        return false;
    }
    
    /**
     * Verifica si los marcos están cargados
     */
    public boolean areFramesLoaded() {
        return framesLoaded;
    }
    
    /**
     * Obtiene una región del atlas de UI
     */
    public TextureRegion getUIRegion(String name) {
        if (uiAtlas == null) return null;
        return uiAtlas.findRegion(name);
    }
    
    /**
     * Obtiene la textura del reverso de carta
     */
    public Texture getCardBackTexture() {
        return cardBackTexture;
    }
    
    /**
     * Carga una textura de personaje (lazy loading)
     * @param characterId ID del personaje (0-49)
     * @param variant Variante (0=base, 1=☆, 2=☆☆, 3=☆☆☆)
     * @return La textura cargada o null si no existe
     */
    public Texture getCharacterTexture(int characterId, int variant) {
        // Validar parámetros
        if (characterId < 0 || characterId >= Constants.TOTAL_CHARACTERS) {
            Gdx.app.error(Constants.TAG, "ID de personaje inválido: " + characterId);
            return null;
        }
        
        if (variant < 0 || variant >= Constants.ARTS_PER_CHARACTER) {
            Gdx.app.error(Constants.TAG, "Variante inválida: " + variant);
            return null;
        }
        
        String key = "char_" + characterId + "_" + variant;
        
        // Si ya está en cache, devolverla
        if (characterTextures.containsKey(key)) {
            return characterTextures.get(key);
        }
        
        // Construir path del archivo
        // Formato: characters/char_XX_V.webp (o .png)
        String fileName = "char_" + String.format("%02d", characterId) + "_" + variant;
        String basePath = Constants.PATH_CHARACTERS + fileName;
        
        // Intentar primero WebP, luego PNG
        String path = basePath + ".webp";
        if (!Gdx.files.internal(path).exists()) {
            path = basePath + ".png";
        }
        
        if (!Gdx.files.internal(path).exists()) {
            // No es error crítico, puede que aún no exista el asset
            Gdx.app.log(Constants.TAG, "Textura de personaje no encontrada: " + fileName);
            return null;
        }
        
        try {
            Texture texture = new Texture(Gdx.files.internal(path));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            characterTextures.put(key, texture);
            Gdx.app.log(Constants.TAG, "Textura cargada: " + key);
            return texture;
        } catch (Exception e) {
            Gdx.app.error(Constants.TAG, "Error cargando textura: " + path + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Descarga una textura de personaje para liberar memoria
     */
    public void unloadCharacterTexture(int characterId, int variant) {
        String key = "char_" + characterId + "_" + variant;
        
        if (characterTextures.containsKey(key)) {
            Texture texture = characterTextures.get(key);
            if (texture != null) {
                texture.dispose();
            }
            characterTextures.remove(key);
            Gdx.app.log(Constants.TAG, "Textura descargada: " + key);
        }
    }
    
    /**
     * Descarga todas las texturas de un personaje
     */
    public void unloadAllVariantsOfCharacter(int characterId) {
        for (int variant = 0; variant < Constants.ARTS_PER_CHARACTER; variant++) {
            unloadCharacterTexture(characterId, variant);
        }
    }
    
    /**
     * Descarga todas las texturas de personajes
     */
    public void unloadAllCharacterTextures() {
        for (Texture texture : characterTextures.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        characterTextures.clear();
        Gdx.app.log(Constants.TAG, "Todas las texturas de personajes descargadas");
    }
    
    /**
     * Precarga las texturas necesarias para un nivel
     * @param characterIds Lista de IDs de personajes a usar
     */
    public void preloadLevelCharacters(int[] characterIds) {
        Gdx.app.log(Constants.TAG, "Precargando " + characterIds.length + " personajes para nivel...");
        for (int id : characterIds) {
            // Solo cargar variante base (0) para gameplay
            getCharacterTexture(id, 0);
        }
    }
    
    /**
     * Obtiene el número de texturas actualmente cargadas
     */
    public int getLoadedTextureCount() {
        return characterTextures.size;
    }
    
    /**
     * Verifica si los assets esenciales están cargados
     */
    public boolean isEssentialLoaded() {
        return uiLoaded;
    }
    
    /**
     * Obtiene información de memoria para debug
     */
    public String getMemoryInfo() {
        return "Texturas cargadas: " + characterTextures.size + 
               " | Frames: " + (hasAnyFrameLoaded() ? "Sí" : "No") +
               " | CardBack: " + (cardBackTexture != null ? "Sí" : "No");
    }
    
    @Override
    public void dispose() {
        Gdx.app.log(Constants.TAG, "Disposing AssetManager...");
        
        // Liberar atlas UI
        if (uiAtlas != null) {
            uiAtlas.dispose();
            uiAtlas = null;
        }
        
        // Liberar card back
        if (cardBackTexture != null) {
            cardBackTexture.dispose();
            cardBackTexture = null;
        }
        
        // Liberar marcos
        for (int i = 0; i < frameTextures.length; i++) {
            if (frameTextures[i] != null) {
                frameTextures[i].dispose();
                frameTextures[i] = null;
            }
        }
        
        // Liberar texturas de personajes
        unloadAllCharacterTextures();
        
        uiLoaded = false;
        framesLoaded = false;
        
        Gdx.app.log(Constants.TAG, "AssetManager disposed correctamente");
    }
}