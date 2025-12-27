package com.waifu.memory.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.waifu.memory.utils.Constants;
import com.waifu.memory.utils.PlaceholderTextures;

public class AssetManager implements Disposable {

    private final ObjectMap<String, Texture> characterTextures = new ObjectMap<>();

    private Texture cardBackTexture;
    private Texture logoTexture;
    private final Texture[] frameTextures = new Texture[Constants.VARIANTS_PER_CHARACTER];

    private final PlaceholderTextures placeholders = new PlaceholderTextures();
    private boolean essentialsLoaded = false;

    public void loadEssentialAssets() {
        if (essentialsLoaded) return;

        placeholders.init(Constants.ASSET_CARD_WIDTH, Constants.ASSET_CARD_HEIGHT, Constants.VARIANTS_PER_CHARACTER);

        cardBackTexture = loadIfExists(Constants.getUiPath(Constants.UI_CARD_BACK));
        logoTexture = loadIfExists(Constants.getUiPath(Constants.UI_LOGO));

        for (int v = 0; v < Constants.VARIANTS_PER_CHARACTER; v++) {
            frameTextures[v] = loadIfExists(Constants.getFramePath(v));
        }

        essentialsLoaded = true;
    }

    private Texture loadIfExists(String path) {
        if (path == null) return null;
        if (!Gdx.files.internal(path).exists()) return null;
        try {
            Texture t = new Texture(Gdx.files.internal(path));
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return t;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isEssentialLoaded() {
        return essentialsLoaded;
    }

    public Texture getCardBackTexture() {
        return cardBackTexture != null ? cardBackTexture : placeholders.getCardBack();
    }

    public Texture getLogoTexture() {
        return logoTexture;
    }

    public Texture getFrameTexture(int variant) {
        int v = variant;
        if (v < 0 || v >= Constants.VARIANTS_PER_CHARACTER) v = 0;
        Texture real = frameTextures[v];
        return real != null ? real : placeholders.getFrame(v);
    }

    public Texture getCharacterTexture(int characterId, int variant) {
        if (!Constants.isValidCharacterId(characterId) || !Constants.isValidVariant(variant)) {
            return placeholders.getCharacter();
        }

        String key = characterId + "_" + variant;

        if (characterTextures.containsKey(key)) {
            Texture cached = characterTextures.get(key);
            return cached != null ? cached : placeholders.getCharacter();
        }

        String path = Constants.getCharacterPath(characterId, variant);
        Texture t = loadIfExists(path);

        characterTextures.put(key, t);
        return t != null ? t : placeholders.getCharacter();
    }

    public void unloadCharacterTexture(int characterId, int variant) {
        if (!Constants.isValidCharacterId(characterId) || !Constants.isValidVariant(variant)) return;

        String key = characterId + "_" + variant;
        if (!characterTextures.containsKey(key)) return;

        Texture t = characterTextures.get(key);
        if (t != null) t.dispose();

        characterTextures.remove(key);
    }

    public void unloadCharacter(int characterId) {
        if (!Constants.isValidCharacterId(characterId)) return;
        for (int v = 0; v < Constants.VARIANTS_PER_CHARACTER; v++) {
            unloadCharacterTexture(characterId, v);
        }
    }

    public void unloadAllCharacters() {
        for (Texture t : characterTextures.values()) {
            if (t != null) t.dispose();
        }
        characterTextures.clear();
    }

    @Override
    public void dispose() {
        if (cardBackTexture != null) cardBackTexture.dispose();
        if (logoTexture != null) logoTexture.dispose();

        for (int i = 0; i < frameTextures.length; i++) {
            if (frameTextures[i] != null) frameTextures[i].dispose();
            frameTextures[i] = null;
        }

        unloadAllCharacters();
        placeholders.dispose();

        essentialsLoaded = false;
    }
}