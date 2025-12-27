package com.waifu.memory.systems;

import com.waifu.memory.data.LevelConfig;
import com.waifu.memory.data.PlayerData;

public final class EconomyManager {

    private EconomyManager() {}

    public static class RewardBreakdown {
        public int pairsFound;
        public int pairValue;
        public int pairsReward;
        public int timeBonus;
        public float multiplier;
        public int multiplierBonus;
        public int total;

        public int totalWithRewardedDouble() {
            return total * 2;
        }
    }

    public static int calculateTimeBonus(float secondsRemaining) {
        if (secondsRemaining <= 0f) return 0;
        return (int) (secondsRemaining / 2f);
    }

    public static RewardBreakdown calculateVictoryReward(PlayerData player, LevelConfig cfg, int pairsFound, float secondsRemaining) {
        RewardBreakdown r = new RewardBreakdown();

        int pv = player.getCurrentPairValue();
        r.pairsFound = Math.max(0, pairsFound);
        r.pairValue = pv;

        r.pairsReward = r.pairsFound * pv;
        r.timeBonus = calculateTimeBonus(secondsRemaining);

        int subtotal = r.pairsReward + r.timeBonus;

        float mult = cfg.rewardMultiplier;
        if (mult < 1f) mult = 1f;
        r.multiplier = mult;

        int multiplied = (int) (subtotal * mult);
        r.multiplierBonus = multiplied - subtotal;

        r.total = subtotal + r.multiplierBonus;
        if (r.total < 0) r.total = 0;

        return r;
    }

    public static int applyRewardedDoubleExtra(int alreadyGrantedTotal) {
        if (alreadyGrantedTotal <= 0) return 0;
        return alreadyGrantedTotal;
    }
}