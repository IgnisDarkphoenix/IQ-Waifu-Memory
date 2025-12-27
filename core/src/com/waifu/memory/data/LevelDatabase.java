package com.waifu.memory.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.waifu.memory.utils.Constants;

public class LevelDatabase {

    private boolean loaded = false;
    private final JsonValue[] overrides = new JsonValue[Constants.TOTAL_LEVELS + 1];

    private void loadIfNeeded() {
        if (loaded) return;
        loaded = true;

        String path = Constants.LEVELS_JSON_PATH;
        if (!Gdx.files.internal(path).exists()) {
            return;
        }

        try {
            String text = Gdx.files.internal(path).readString("UTF-8");
            JsonValue root = new JsonReader().parse(text);

            JsonValue levels = root.get("levels");
            if (levels == null) return;

            for (JsonValue lv = levels.child; lv != null; lv = lv.next) {
                if (!lv.has("level")) continue;
                int level = lv.getInt("level", -1);
                if (level >= 1 && level <= Constants.TOTAL_LEVELS) {
                    overrides[level] = lv;
                }
            }
        } catch (Exception ignored) {
        }
    }

    public LevelConfig get(int levelNumber) {
        loadIfNeeded();

        int level = levelNumber;
        if (!Constants.isValidLevel(level)) level = 1;

        LevelConfig cfg = createDefault(level);

        JsonValue ov = overrides[level];
        if (ov != null) {
            applyOverride(cfg, ov);
        }

        cfg.poolCount = cfg.clampPoolCount();
        cfg.shuffleInterval = clampInt(cfg.shuffleInterval, 2, 12);
        cfg.multiGridCount = clampInt(cfg.multiGridCount, 1, 3);

        return cfg;
    }

    private int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private LevelConfig createDefault(int level) {
        LevelConfig cfg = new LevelConfig();
        cfg.level = level;

        if (level <= Constants.LEVELS_EASY_END) {
            cfg.gridSize = Constants.GRID_EASY;
            cfg.timeBonusSeconds = Constants.TIME_BONUS_4X4;
            cfg.rewardMultiplier = Constants.MULTIPLIER_EASY;
            cfg.shuffle = false;
            cfg.shuffleInterval = Constants.SHUFFLE_INTERVAL;
            cfg.fade = false;
            cfg.multiGrid = false;
            cfg.multiGridCount = 1;

        } else if (level <= Constants.LEVELS_NORMAL_END) {
            cfg.gridSize = Constants.GRID_NORMAL;
            cfg.timeBonusSeconds = Constants.TIME_BONUS_6X6;
            cfg.rewardMultiplier = Constants.MULTIPLIER_NORMAL;
            cfg.shuffle = false;
            cfg.shuffleInterval = Constants.SHUFFLE_INTERVAL;
            cfg.fade = false;
            cfg.multiGrid = false;
            cfg.multiGridCount = 1;

        } else {
            cfg.gridSize = Constants.GRID_HARD;
            cfg.timeBonusSeconds = Constants.TIME_BONUS_8X8;
            cfg.rewardMultiplier = Constants.MULTIPLIER_HARD;
            cfg.shuffle = true;
            cfg.shuffleInterval = Constants.SHUFFLE_INTERVAL;
            cfg.fade = false;
            cfg.multiGrid = false;
            cfg.multiGridCount = 1;
        }

        int pairsNeeded = (cfg.gridSize * cfg.gridSize) / 2;
        int progressive = 8 + (level * (Constants.TOTAL_CHARACTERS - 8) / Constants.TOTAL_LEVELS);
        cfg.poolCount = Math.max(pairsNeeded, Math.min(Constants.TOTAL_CHARACTERS, progressive));

        return cfg;
    }

    private void applyOverride(LevelConfig cfg, JsonValue ov) {
        if (ov.has("gridSize")) cfg.gridSize = ov.getInt("gridSize", cfg.gridSize);
        if (ov.has("timeBonusSeconds")) cfg.timeBonusSeconds = ov.getInt("timeBonusSeconds", cfg.timeBonusSeconds);
        if (ov.has("rewardMultiplier")) cfg.rewardMultiplier = ov.getFloat("rewardMultiplier", cfg.rewardMultiplier);

        if (ov.has("shuffle")) cfg.shuffle = ov.getBoolean("shuffle", cfg.shuffle);
        if (ov.has("shuffleInterval")) cfg.shuffleInterval = ov.getInt("shuffleInterval", cfg.shuffleInterval);

        if (ov.has("multiGrid")) cfg.multiGrid = ov.getBoolean("multiGrid", cfg.multiGrid);
        if (ov.has("multiGridCount")) cfg.multiGridCount = ov.getInt("multiGridCount", cfg.multiGridCount);

        if (ov.has("fade")) cfg.fade = ov.getBoolean("fade", cfg.fade);

        if (ov.has("poolCount")) cfg.poolCount = ov.getInt("poolCount", cfg.poolCount);
    }
}