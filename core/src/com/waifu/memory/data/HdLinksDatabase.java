package com.waifu.memory.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.waifu.memory.utils.Constants;

public class HdLinksDatabase {

    private boolean loaded = false;
    private final IntMap<String> links = new IntMap<>();

    private void loadIfNeeded() {
        if (loaded) return;
        loaded = true;

        String path = Constants.HD_LINKS_JSON_PATH;
        if (!Gdx.files.internal(path).exists()) {
            return;
        }

        try {
            String text = Gdx.files.internal(path).readString("UTF-8");
            JsonValue root = new JsonReader().parse(text);

            JsonValue linksNode = root.get("links");
            if (linksNode == null) return;

            for (JsonValue child = linksNode.child; child != null; child = child.next) {
                String key = child.name;
                String url = child.asString();
                if (url == null || url.isEmpty()) continue;

                try {
                    int id = Integer.parseInt(key);
                    if (Constants.isValidCharacterId(id)) {
                        links.put(id, url);
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
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