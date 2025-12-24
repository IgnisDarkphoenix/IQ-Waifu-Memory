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
    
    // Estado de carga
    private boolean uiLoaded;
    
    public AssetManager() {
        characterTextures = new ObjectMap<>();
        uiLoaded = false;
    }
    
    /**
     * Carga los assets esenciales de UI
     * Llamar en SplashScreen
     */
    public void loadEssentialAssets() {
        Gdx.app.log(Constants.TAG, "Cargando assets esenciales...");
        
        try {
            // Cargar atlas de UI
            if (Gdx.files.internal(Constants.PATH_UI + Constants.ATLAS_UI).exists()) {
                uiAtlas = new TextureAtlas(Gdx.files.internal(Constants.PATH_UI + Constants.ATLAS_UI));
            }
            
            // Cargar textura del reverso de carta
            String cardBackPath = Constants.PATH_UI + "card_back.png";
            if (Gdx.files.internal(cardBackPath).exists()) {
                cardBackTexture = new Texture(Gdx.files.internal(cardBackPath));
                cardBackTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            }
            
            uiLoaded = true;
            Gdx.app.log(Constants.TAG, "Assets esenciales cargados");
            
        } catch (Exception e) {
            Gdx.app.error(Constants.TAG, "Error cargando assets: " + e.getMessage());
        }
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
        String key = "char_" + characterId + "_" + variant;
        
        // Si ya está en cache, devolverla
        if (characterTextures.containsKey(key)) {
            return characterTextures.get(key);
        }
        
        // Construir path del archivo
        // Formato: characters/char_XX_V.webp (o .png)
        String basePath = Constants.PATH_CHARACTERS + "char_" + 
                         String.format("%02d", characterId) + "_" + variant;
        
        String path = basePath + ".webp";
        if (!Gdx.files.internal(path).exists()) {
            path = basePath + ".png";
        }
        
        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.log(Constants.TAG, "Textura no encontrada: " + path);
            return null;
        }
        
        try {
            Texture texture = new Texture(Gdx.files.internal(path));
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            characterTextures.put(key, texture);
            Gdx.app.log(Constants.TAG, "Textura cargada: " + key);
            return texture;
        } catch (Exception e) {
            Gdx.app.error(Constants.TAG, "Error cargando textura: " + path);
            return null;
        }
    }
    
    /**
     * Descarga una textura de personaje para liberar memoria
     */
    public void unloadCharacterTexture(int characterId, int variant) {
        String key = "char_" + characterId + "_" + variant;
        
        if (characterTextures.containsKey(key)) {
            characterTextures.get(key).dispose();
            characterTextures.remove(key);
            Gdx.app.log(Constants.TAG, "Textura descargada: " + key);
        }
    }
    
    /**
     * Descarga todas las texturas de personajes
     */
    public void unloadAllCharacterTextures() {
        for (Texture texture : characterTextures.values()) {
            texture.dispose();
        }
        characterTextures.clear();
        Gdx.app.log(Constants.TAG, "Todas las texturas de personajes descargadas");
    }
    
    /**
     * Precarga las texturas necesarias para un nivel
     * @param characterIds Lista de IDs de personajes a usar
     */
    public void preloadLevelCharacters(int[] characterIds) {
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
    
    @Override
    public void dispose() {
        if (uiAtlas != null) {
            uiAtlas.dispose();
        }
        
        if (cardBackTexture != null) {
            cardBackTexture.dispose();
        }
        
        unloadAllCharacterTextures();
        
        Gdx.app.log(Constants.TAG, "AssetManager disposed");
    }
}