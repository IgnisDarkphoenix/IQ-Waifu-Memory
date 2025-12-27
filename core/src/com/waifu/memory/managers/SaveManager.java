package com.waifu.memory.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.waifu.memory.data.PlayerData;
import com.waifu.memory.utils.Constants;

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
                return;
            }

            PlayerData loaded = json.fromJson(PlayerData.class, jsonData);
            if (loaded == null) {
                loadBackupOrReset();
                return;
            }

            playerData = loaded;
            if (playerData.galleryUnlocks == null) playerData.galleryUnlocks = new java.util.HashMap<>();
        } catch (Exception e) {
            Gdx.app.error(Constants.TAG, "Load failed: " + e.getMessage());
            loadBackupOrReset();
        }
    }

    private void loadBackupOrReset() {
        try {
            String backup = prefs.getString(KEY_BACKUP, "");
            if (backup != null && !backup.isEmpty()) {
                PlayerData loaded = json.fromJson(PlayerData.class, backup);
                if (loaded != null) {
                    playerData = loaded;
                    if (playerData.galleryUnlocks == null) playerData.galleryUnlocks = new java.util.HashMap<>();
                    return;
                }
            }
        } catch (Exception ignored) {
        }
        playerData = new PlayerData();
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
        } catch (Exception e) {
            Gdx.app.error(Constants.TAG, "Save failed: " + e.getMessage());
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