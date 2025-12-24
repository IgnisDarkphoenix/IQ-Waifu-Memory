package com.waifu.memory.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.waifu.memory.utils.Constants;

/**
 * Manager para cargar y gestionar assets gráficos.
 * Implementa lazy loading para optimizar memoria.
 *
 * Nota: libGDX NO soporta WebP de forma nativa en core. Si colocas .webp,
 * puede fallar al decodificar. Por eso aquí usamos PNG como formato principal.
 * Si necesitas WebP, considera usar gdx-webp extension o convertir a PNG.
 */
public class AssetManager implements Disposable {

    // ========== CONSTANTES ==========
    private static final String[] SUPPORTED_EXTENSIONS = {"png", "jpg", "jpeg"};

    // ========== ATLAS Y TEXTURAS ==========
    // Atlas de UI (siempre cargado)
    private TextureAtlas uiAtlas;

    // Cache de texturas de personajes (lazy loading)
    private final ObjectMap<String, Texture> characterTextures;

    // Textura del reverso de carta
    private Texture cardBackTexture;

    // Marcos por rareza (0=Base, 1=☆, 2=☆☆, 3=☆☆☆)
    private final Texture[] frameTextures;

    // ========== ESTADO ==========
    private boolean essentialsLoaded;
    private boolean framesLoaded;

    // ========== CONSTRUCTOR ==========
    public AssetManager() {
        characterTextures = new ObjectMap<>();
        frameTextures = new Texture[Constants.ARTS_PER_CHARACTER]; // 4
        essentialsLoaded = false;
        framesLoaded = false;
    }

    // ========== CARGA DE ASSETS ESENCIALES ==========

    /**
     * Carga los assets esenciales (UI, reverso, marcos).
     * Llamar en SplashScreen.
     */
    public void loadEssentialAssets() {
        Gdx.app.log(Constants.TAG, "Cargando assets esenciales...");

        try {
            // Cargar atlas de UI (opcional)
            loadUIAtlas();

            // Cargar textura del reverso de carta (opcional pero recomendado)
            loadCardBackTexture();

            // Cargar marcos de rareza (opcional pero recomendado)
            loadFrameTextures();

            essentialsLoaded = true;
            Gdx.app.log(Constants.TAG, "Assets esenciales cargados correctamente");

        } catch (Exception e) {
            Gdx.app.error(Constants.TAG, "Error cargando assets esenciales: " + e.getMessage(), e);
            essentialsLoaded = false;
        }
    }

    /**
     * Carga el atlas de UI
     */
    private void loadUIAtlas() {
        String atlasPath = Constants.PATH_UI + Constants.ATLAS_UI;
        FileHandle atlasFile = Gdx.files.internal(atlasPath);

        if (atlasFile.exists()) {
            uiAtlas = new TextureAtlas(atlasFile);
            Gdx.app.log(Constants.TAG, "Atlas UI cargado: " + atlasPath);
        } else {
            Gdx.app.log(Constants.TAG, "Atlas UI no encontrado (opcional): " + atlasPath);
        }
    }

    /**
     * Carga la textura del reverso de carta
     */
    private void loadCardBackTexture() {
        String cardBackPath = Constants.PATH_UI + "card_back.png";
        FileHandle cardBackFile = Gdx.files.internal(cardBackPath);

        if (cardBackFile.exists()) {
            cardBackTexture = new Texture(cardBackFile);
            cardBackTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            Gdx.app.log(Constants.TAG, "Card back cargado: " + cardBackPath);
        } else {
            Gdx.app.log(Constants.TAG, "Card back no encontrado: " + cardBackPath);
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
            FileHandle fileHandle = Gdx.files.internal(path);

            if (!fileHandle.exists()) {
                frameTextures[i] = null;
                Gdx.app.log(Constants.TAG, "Marco no encontrado (opcional): " + path);
                continue;
            }

            try {
                Texture texture = new Texture(fileHandle);
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                frameTextures[i] = texture;
                loadedCount++;
                Gdx.app.log(Constants.TAG, "Marco cargado: " + frameFiles[i]);
            } catch (Exception e) {
                frameTextures[i] = null;
                Gdx.app.error(Constants.TAG, "Error cargando marco: " + path, e);
            }
        }

        framesLoaded = true;
        Gdx.app.log(Constants.TAG, "Marcos cargados: " + loadedCount + "/" + frameFiles.length);
    }

    // ========== GETTERS DE ASSETS BÁSICOS ==========

    /**
     * Obtiene una región del atlas de UI
     * @param name Nombre de la región
     * @return TextureRegion o null si no existe
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
     * Obtiene la textura del marco por rareza/variante
     * @param rarity 0=Base, 1=☆, 2=☆☆, 3=☆☆☆
     * @return Textura del marco o null si no existe
     */
    public Texture getFrameTexture(int rarity) {
        if (rarity < 0 || rarity >= frameTextures.length) {
            return null;
        }
        return frameTextures[rarity];
    }

    // ========== CARGA DE PERSONAJES (LAZY LOADING) ==========

    /**
     * Carga una textura de personaje (lazy loading)
     * @param characterId ID del personaje (0-49)
     * @param variant Variante (0=Base, 1=☆, 2=☆☆, 3=☆☆☆)
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

        String key = buildCharacterKey(characterId, variant);

        // Si ya está en cache, devolverla
        if (characterTextures.containsKey(key)) {
            return characterTextures.get(key);
        }

        // Construir path base del archivo
        // Formato: characters/char_XX_V.png
        String basePath = Constants.PATH_CHARACTERS 
            + "char_" + String.format("%02d", characterId) + "_" + variant;

        // Intentar cargar con las extensiones soportadas
        Texture texture = loadTextureBestEffort(basePath);

        if (texture == null) {
            Gdx.app.log(Constants.TAG, "Textura de personaje no encontrada: " + basePath);
            return null;
        }

        characterTextures.put(key, texture);
        Gdx.app.log(Constants.TAG, "Textura cargada: " + key);
        return texture;
    }

    /**
     * Intenta cargar una textura probando diferentes extensiones
     * @param basePathNoExt Path sin extensión
     * @return Texture cargada o null si falla
     */
    private Texture loadTextureBestEffort(String basePathNoExt) {
        for (String ext : SUPPORTED_EXTENSIONS) {
            String path = basePathNoExt + "." + ext;
            FileHandle fileHandle = Gdx.files.internal(path);

            if (!fileHandle.exists()) {
                continue;
            }

            try {
                Texture texture = new Texture(fileHandle);
                texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                return texture;
            } catch (Exception e) {
                Gdx.app.error(Constants.TAG, "Error decodificando textura: " + path, e);
            }
        }
        return null;
    }

    /**
     * Genera la clave de cache para un personaje
     */
    private String buildCharacterKey(int characterId, int variant) {
        return "char_" + characterId + "_" + variant;
    }

    // ========== PRECARGA Y DESCARGA ==========

    /**
     * Precarga las texturas necesarias para un nivel
     * @param characterIds Array de IDs de personajes a usar
     */
    public void preloadLevelCharacters(int[] characterIds) {
        Gdx.app.log(Constants.TAG, "Precargando " + characterIds.length + " personajes para nivel...");
        for (int id : characterIds) {
            // Solo cargar variante base (0) para gameplay
            getCharacterTexture(id, 0);
        }
    }

    /**
     * Descarga una textura de personaje para liberar memoria
     */
    public void unloadCharacterTexture(int characterId, int variant) {
        String key = buildCharacterKey(characterId, variant);

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
     * Descarga todas las variantes de un personaje
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

    // ========== ESTADO Y DEBUG ==========

    /**
     * Verifica si los assets esenciales están cargados
     */
    public boolean isEssentialLoaded() {
        return essentialsLoaded;
    }

    /**
     * Verifica si los marcos están cargados
     */
    public boolean areFramesLoaded() {
        return framesLoaded;
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
     * Obtiene el número de texturas de personajes actualmente cargadas
     */
    public int getLoadedCharacterTextureCount() {
        return characterTextures.size;
    }

    /**
     * Obtiene información de memoria para debug
     */
    public String getMemoryInfo() {
        int framesCount = 0;
        for (Texture frame : frameTextures) {
            if (frame != null) framesCount++;
        }

        return String.format(
            "Personajes: %d | Marcos: %d/%d | CardBack: %s | UI Atlas: %s",
            characterTextures.size,
            framesCount,
            frameTextures.length,
            cardBackTexture != null ? "Sí" : "No",
            uiAtlas != null ? "Sí" : "No"
        );
    }

    // ========== DISPOSE ==========

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

        essentialsLoaded = false;
        framesLoaded = false;

        Gdx.app.log(Constants.TAG, "AssetManager disposed correctamente");
    }
}