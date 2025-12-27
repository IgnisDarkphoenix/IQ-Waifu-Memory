package com.waifu.memory.data;

import com.waifu.memory.utils.Constants;

public class LevelConfig {

    public int level;

    public int gridSize;
    public int timeBonusSeconds;
    public float rewardMultiplier;

    public boolean shuffle;
    public int shuffleInterval;

    public boolean multiGrid;
    public int multiGridCount;

    public boolean fade;

    public int poolCount;

    public LevelConfig() {
        level = 1;
        gridSize = Constants.GRID_EASY;
        timeBonusSeconds = 0;
        rewardMultiplier = 1.0f;
        shuffle = false;
        shuffleInterval = Constants.SHUFFLE_INTERVAL;
        multiGrid = false;
        multiGridCount = 1;
        fade = false;
        poolCount = 8;
    }

    public int totalPairs() {
        return (gridSize * gridSize) / 2;
    }

    public int clampPoolCount() {
        int minNeeded = totalPairs();
        int c = poolCount;
        if (c < minNeeded) c = minNeeded;
        if (c > Constants.TOTAL_CHARACTERS) c = Constants.TOTAL_CHARACTERS;
        return c;
    }
}