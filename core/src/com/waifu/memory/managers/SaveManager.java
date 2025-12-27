package com.waifu.memory.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.waifu.memory.data.PlayerData;
import com.waifu.memory.utils.Constants;
import java.util.HashMap;

public class SaveManager {

    private static final String PREFS_NAME = "IQWaifuMemorySave";
    private static final String KEY_PLAYER_DATA = "playerData";
    private static final String KEY_BACKUP = "playerDataBackup";

    private final Preferences prefs;
    private PlayerData playerData;
    private final Json json;

    public SaveManager() {
        prefs = Gdx.app.getPreferences(PREFS_NAME);
        json = new Json();
        json.setIgnoreUnknownFields(true);
        json.setOutputType(JsonWriter.OutputType.json);
        playerData = new PlayerData();
    }

    public void load() {
        try {
            String jsonData = prefs.getString(KEY_PLAYER_DATA, "");
            if (jsonData == null || jsonData.isEmpty()) {
                playerData = new PlayerData();
                ensureMaps();
                return;
            }

            PlayerData loaded = json.fromJson(PlayerData.class, jsonData);
            if (loaded == null) {
                loadBackupOrReset();
                ensureMaps();
                return;
            }

            playerData = loaded;
            ensureMaps();
        } catch (Exception e) {
            loadBackupOrReset();
            ensureMaps();
        }
    }

    private void loadBackupOrReset() {
        try {
            String backup = prefs.getString(KEY_BACKUP, "");
            if (backup != null && !backup.isEmpty()) {
                PlayerData loaded = json.fromJson(PlayerData.class, backup);
                if (loaded != null) {
                    playerData = loaded;
                    return;
                }
            }
        } catch (Exception ignored) {
        }
        playerData = new PlayerData();
    }

    private void ensureMaps() {
        if (playerData.galleryUnlocks == null) playerData.galleryUnlocks = new HashMap<>();
    }

    public void save() {
        try {
            String previous = prefs.getString(KEY_PLAYER_DATA, "");
            if (previous != null && !previous.isEmpty()) {
                prefs.putString(KEY_BACKUP, previous);
            }

            String jsonData = json.toJson(playerData);
            prefs.putString(KEY_PLAYER_DATA, jsonData);
            prefs.flush();
        } catch (Exception ignored) {
        }
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public void resetAll() {
        playerData = new PlayerData();
        prefs.remove(KEY_PLAYER_DATA);
        prefs.remove(KEY_BACKUP);
        prefs.flush();
    }

    public boolean hasSaveData() {
        String jsonData = prefs.getString(KEY_PLAYER_DATA, "");
        return jsonData != null && !jsonData.isEmpty();
    }
}