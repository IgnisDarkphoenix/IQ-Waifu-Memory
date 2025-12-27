package com.waifu.memory.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.IntMap;
import com.waifu.memory.utils.Constants;

public class HdLinksDatabase {

    private boolean loaded = false;
    private final IntMap<String> links = new IntMap<>();

    public void loadIfNeeded() {
        if (loaded) return;
        loaded = true;

        String path = Constants.HD_LINKS_JSON_PATH;
        if (!Gdx.files.internal(path).exists()) {
            Gdx.app.log(Constants.TAG, "hd_links.json no existe: " + path);
            return;
        }

        try {
            String text = Gdx.files.internal(path).readString("UTF-8");
            JsonValue root = new JsonReader().parse(text);

            JsonValue linksNode = root.get("links");
            if (linksNode != null) {
                for (JsonValue child = linksNode.child; child != null; child = child.next) {
                    String key = child.name;
                    String url = child.asString();
                    try {
                        int id = Integer.parseInt(key);
                        if (Constants.isValidCharacterId(id) && url != null && !url.isEmpty()) {
                            links.put(id, url);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            Gdx.app.log(Constants.TAG, "HdLinks cargados: " + links.size);
        } catch (Exception e) {
            Gdx.app.error(Constants.TAG, "Error leyendo hd_links.json: " + e.getMessage());
        }
    }

    public boolean hasUrlFor(int characterId) {
        loadIfNeeded();
        return links.containsKey(characterId);
    }

    public String getUrl(int characterId) {
        loadIfNeeded();
        return links.get(characterId);
    }
}