package com.waifu.memory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.waifu.memory.IQWaifuMemory;
import com.waifu.memory.data.LevelConfig;
import com.waifu.memory.data.LevelDatabase;
import com.waifu.memory.entities.Card;
import com.waifu.memory.entities.GameGrid;
import com.waifu.memory.systems.EconomyManager;
import com.waifu.memory.utils.Constants;

public class GameScreen extends BaseScreen {

    private final int levelNumber;

    private final LevelDatabase levelDatabase;
    private final LevelConfig levelConfig;

    private enum GameState {
        PLAYING,
        CHECKING_MATCH,
        VICTORY,
        DEFEAT,
        PAUSED
    }

    private GameState gameState;

    private GameGrid gameGrid;

    private Card firstCard;
    private Card secondCard;
    private float checkTimer;

    private float gameTime;
    private float maxTime;
    private boolean timerWarning;

    private int pairsFound;
    private int pcoinsEarned;

    private int cardsFlippedSinceShuffle;
    private boolean shuffleEnabled;
    private int shuffleInterval;

    private boolean hintEnabled;
    private int hintsLeft;
    private Rectangle hintButton;

    private EconomyManager.RewardBreakdown victoryReward;
    private boolean rewardedDoubleClaimed;

    private BitmapFont hudFont;
    private BitmapFont bigFont;
    private GlyphLayout layout;
    private ShapeRenderer shapeRenderer;

    private Rectangle pauseButton;

    private boolean showingPopup;
    private Rectangle[] popupButtons;
    private String[] popupButtonTexts;

    private Vector3 touchPos;

    public GameScreen(IQWaifuMemory game, int levelNumber) {
        super(game);

        this.levelNumber = levelNumber;
        this.gameState = GameState.PLAYING;

        this.levelDatabase = new LevelDatabase();
        this.levelConfig = levelDatabase.get(levelNumber);

        setupLevelFromConfig();

        hudFont = new BitmapFont();
        hudFont.getData().setScale(2.5f);
        hudFont.setColor(Color.WHITE);

        bigFont = new BitmapFont();
        bigFont.getData().setScale(4f);
        bigFont.setColor(Color.WHITE);

        layout = new GlyphLayout();
        shapeRenderer = new ShapeRenderer();
        touchPos = new Vector3();

        pauseButton = new Rectangle(40, Constants.WORLD_HEIGHT - 100, 80, 80);

        hintEnabled = Constants.isHintsEnabledForGrid(levelConfig.gridSize);
        hintsLeft = hintEnabled ? Constants.HINTS_PER_MATCH : 0;
        hintButton = new Rectangle(Constants.WORLD_WIDTH - 220f, Constants.WORLD_HEIGHT - 170f, 180f, 45f);

        rewardedDoubleClaimed = false;
        victoryReward = null;

        setupInput();

        Gdx.app.log(Constants.TAG, "Nivel " + levelNumber + " cfg: grid=" + levelConfig.gridSize +
            " shuffle=" + levelConfig.shuffle + " interval=" + levelConfig.shuffleInterval +
            " pool=" + levelConfig.poolCount + " mult=" + levelConfig.rewardMultiplier);
    }

    public com.waifu.memory.managers.AssetManager getAssetManager() {
        return assetManager;
    }

    private void setupLevelFromConfig() {
        shuffleEnabled = levelConfig.shuffle;
        shuffleInterval = levelConfig.shuffleInterval > 0 ? levelConfig.shuffleInterval : Constants.SHUFFLE_INTERVAL;

        int baseTime = getPlayerData().getCurrentBaseTime();
        maxTime = baseTime + levelConfig.timeBonusSeconds;
        gameTime = maxTime;

        int[] pool = buildPool(levelConfig.poolCount);

        gameGrid = new GameGrid(levelConfig.gridSize, this, pool);

        firstCard = null;
        secondCard = null;
        checkTimer = 0f;

        pairsFound = 0;
        pcoinsEarned = 0;

        cardsFlippedSinceShuffle = 0;
        showingPopup = false;
        timerWarning = false;
    }

    private int[] buildPool(int poolCount) {
        int pairsNeeded = levelConfig.totalPairs();
        int c = poolCount;

        if (c < pairsNeeded) c = pairsNeeded;
        if (c < 1) c = 1;
        if (c > Constants.TOTAL_CHARACTERS) c = Constants.TOTAL_CHARACTERS;

        int[] pool = new int[c];
        for (int i = 0; i < c; i++) pool[i] = i;
        return pool;
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY, 0);
                viewport.unproject(touchPos);

                if (showingPopup) {
                    handlePopupInput();
                    return true;
                }

                if (pauseButton.contains(touchPos.x, touchPos.y)) {
                    audioManager.playButtonClick();
                    togglePause();
                    return true;
                }

                if (hintEnabled && hintsLeft > 0 && gameState == GameState.PLAYING) {
                    if (hintButton.contains(touchPos.x, touchPos.y)) {
                        audioManager.playButtonClick();
                        showRewardedAdForHint();
                        return true;
                    }
                }

                if (gameState == GameState.PLAYING) {
                    handleCardClick();
                }

                return true;
            }
        });
    }

    private void handleCardClick() {
        Card clickedCard = gameGrid.getCardAt(touchPos.x, touchPos.y);

        if (clickedCard == null) return;
        if (clickedCard.isMatched()) return;
        if (clickedCard.isRevealed()) return;
        if (clickedCard.isAnimating()) return;
        if (clickedCard == firstCard) return;

        clickedCard.flip();
        audioManager.playCardFlip();
        cardsFlippedSinceShuffle++;

        if (firstCard == null) {
            firstCard = clickedCard;
        } else {
            secondCard = clickedCard;
            gameState = GameState.CHECKING_MATCH;
            checkTimer = Constants.CARD_SHOW_TIME;
        }
    }

    private void handlePopupInput() {
        if (popupButtons == null) return;

        for (int i = 0; i < popupButtons.length; i++) {
            if (popupButtons[i].contains(touchPos.x, touchPos.y)) {
                audioManager.playButtonClick();
                handlePopupButtonClick(i);
                return;
            }
        }
    }

    private void handlePopupButtonClick(int buttonIndex) {
        if (gameState == GameState.VICTORY) {
            switch (buttonIndex) {
                case 0:
                    showRewardedAdForDouble();
                    break;
                case 1:
                    maybeShowInterstitial();
                    goToScreen(new UpgradesScreen(game));
                    break;
                case 2:
                    maybeShowInterstitial();
                    if (levelNumber < Constants.TOTAL_LEVELS) goToScreen(new GameScreen(game, levelNumber + 1));
                    else goToScreen(new HomeScreen(game));
                    break;
                case 3:
                    maybeShowInterstitial();
                    goToScreen(new HomeScreen(game));
                    break;
            }
        } else if (gameState == GameState.DEFEAT) {
            switch (buttonIndex) {
                case 0:
                    showRewardedAdForTime();
                    break;
                case 1:
                    maybeShowInterstitial();
                    goToScreen(new GameScreen(game, levelNumber));
                    break;
                case 2:
                    maybeShowInterstitial();
                    goToScreen(new HomeScreen(game));
                    break;
            }
        } else if (gameState == GameState.PAUSED) {
            switch (buttonIndex) {
                case 0:
                    togglePause();
                    break;
                case 1:
                    maybeShowInterstitial();
                    goToScreen(new GameScreen(game, levelNumber));
                    break;
                case 2:
                    maybeShowInterstitial();
                    goToScreen(new HomeScreen(game));
                    break;
            }
        }
    }

    private void maybeShowInterstitial() {
        if (!game.hasAdHandler()) return;
        if (!getPlayerData().shouldShowInterstitial()) return;

        game.getAdHandler().showInterstitialAd();
        getPlayerData().recordInterstitialShown();
        saveProgress();
    }

    private void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            showPausePopup();
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
            showingPopup = false;
        }
    }

    private void showRewardedAdForDouble() {
        if (rewardedDoubleClaimed) return;
        if (gameState != GameState.VICTORY) return;
        if (victoryReward == null) return;

        if (!game.hasAdHandler() || !game.getAdHandler().isRewardedAdLoaded()) return;

        game.getAdHandler().showRewardedAd(new IQWaifuMemory.RewardCallback() {
            @Override
            public void onRewardEarned() {
                int extra = EconomyManager.applyRewardedDoubleExtra(victoryReward.total);
                getPlayerData().addPcoins(extra);
                getPlayerData().recordRewardedWatched();

                pcoinsEarned = victoryReward.totalWithRewardedDouble();
                rewardedDoubleClaimed = true;

                saveProgress();
            }

            @Override
            public void onAdFailed() {
                Gdx.app.log(Constants.TAG, "Ad failed to show");
            }
        });
    }

    private void showRewardedAdForTime() {
        if (!game.hasAdHandler() || !game.getAdHandler().isRewardedAdLoaded()) return;

        game.getAdHandler().showRewardedAd(new IQWaifuMemory.RewardCallback() {
            @Override
            public void onRewardEarned() {
                gameTime = Constants.AD_EXTRA_TIME;
                timerWarning = false;
                gameState = GameState.PLAYING;
                showingPopup = false;

                getPlayerData().recordRewardedWatched();
                saveProgress();
            }

            @Override
            public void onAdFailed() {
                Gdx.app.log(Constants.TAG, "Ad failed to show");
            }
        });
    }

    private void showRewardedAdForHint() {
        if (!hintEnabled || hintsLeft <= 0) return;
        if (!game.hasAdHandler() || !game.getAdHandler().isRewardedAdLoaded()) return;

        game.getAdHandler().showRewardedAd(new IQWaifuMemory.RewardCallback() {
            @Override
            public void onRewardEarned() {
                boolean ok = gameGrid.triggerHintShake();
                if (ok) {
                    hintsLeft--;
                    getPlayerData().recordHintUsed();
                }

                getPlayerData().recordRewardedWatched();
                saveProgress();
            }

            @Override
            public void onAdFailed() {
                Gdx.app.log(Constants.TAG, "Ad failed to show");
            }
        });
    }

    @Override
    protected void update(float delta) {
        if (gameState == GameState.PLAYING) {
            gameTime -= delta;

            if (gameTime <= 10f && !timerWarning) {
                timerWarning = true;
                audioManager.playTimerWarning();
            }

            if (gameTime <= 0f) {
                gameTime = 0f;
                onDefeat();
                return;
            }

            gameGrid.update(delta);

        } else if (gameState == GameState.CHECKING_MATCH) {
            checkTimer -= delta;
            if (checkTimer <= 0f) checkMatch();
        }
    }

    private void checkMatch() {
        if (firstCard != null && secondCard != null && firstCard.getCharacterId() == secondCard.getCharacterId()) {
            firstCard.setMatched(true);
            secondCard.setMatched(true);
            pairsFound++;

            pcoinsEarned = pairsFound * getPlayerData().getCurrentPairValue();

            audioManager.playMatch();
            audioManager.playCoinCollect();

            if (gameGrid.isAllMatched()) {
                onVictory();
                return;
            }
        } else {
            if (firstCard != null) firstCard.flipBack();
            if (secondCard != null) secondCard.flipBack();
            audioManager.playNoMatch();
        }

        if (shuffleEnabled && cardsFlippedSinceShuffle >= shuffleInterval) {
            gameGrid.shuffleUnmatched();
            cardsFlippedSinceShuffle = 0;
        }

        firstCard = null;
        secondCard = null;
        gameState = GameState.PLAYING;
    }

    private void onVictory() {
        gameState = GameState.VICTORY;
        audioManager.playVictory();

        victoryReward = EconomyManager.calculateVictoryReward(
            getPlayerData(),
            levelConfig,
            pairsFound,
            gameTime
        );

        pcoinsEarned = victoryReward.total;
        getPlayerData().addPcoins(victoryReward.total);

        if (levelNumber > getPlayerData().maxLevelCompleted) {
            getPlayerData().maxLevelCompleted = levelNumber;
        }

        getPlayerData().recordGamePlayed(true, pairsFound);
        saveProgress();

        showVictoryPopup();
    }

    private void onDefeat() {
        gameState = GameState.DEFEAT;
        audioManager.playDefeat();

        pcoinsEarned = 0;

        getPlayerData().recordGamePlayed(false, pairsFound);
        saveProgress();

        showDefeatPopup();
    }

    private void showVictoryPopup() {
        showingPopup = true;
        popupButtonTexts = new String[]{"x2 AD", "MEJORAS", "SIGUIENTE", "HOME"};
        createPopupButtons(4);
    }

    private void showDefeatPopup() {
        showingPopup = true;
        popupButtonTexts = new String[]{"+15s AD", "REINTENTAR", "HOME"};
        createPopupButtons(3);
    }

    private void showPausePopup() {
        showingPopup = true;
        popupButtonTexts = new String[]{"CONTINUAR", "REINICIAR", "SALIR"};
        createPopupButtons(3);
    }

    private void createPopupButtons(int count) {
        popupButtons = new Rectangle[count];

        float buttonWidth = 280f;
        float buttonHeight = 80f;
        float spacing = 20f;
        float startY = Constants.WORLD_HEIGHT / 2 - 100;
        float centerX = Constants.WORLD_WIDTH / 2;

        for (int i = 0; i < count; i++) {
            popupButtons[i] = new Rectangle(
                centerX - buttonWidth / 2,
                startY - i * (buttonHeight + spacing),
                buttonWidth,
                buttonHeight
            );
        }
    }

    @Override
    protected void draw() {
        gameGrid.draw(batch, shapeRenderer, camera);
        drawHUD();
        if (showingPopup) drawPopup();
    }

    private void drawHUD() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.15f, 0.15f, 0.25f, 0.9f);
        shapeRenderer.rect(0, Constants.WORLD_HEIGHT - 120, Constants.WORLD_WIDTH, 120);

        shapeRenderer.setColor(0.3f, 0.3f, 0.4f, 1f);
        shapeRenderer.rect(pauseButton.x, pauseButton.y, pauseButton.width, pauseButton.height);

        float timeBarWidth = 400f;
        float timeBarHeight = 30f;
        float timeBarX = Constants.WORLD_WIDTH - timeBarWidth - 40;
        float timeBarY = Constants.WORLD_HEIGHT - 80;
        float timePercent = maxTime <= 0 ? 0f : (gameTime / maxTime);

        shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
        shapeRenderer.rect(timeBarX, timeBarY, timeBarWidth, timeBarHeight);

        if (timerWarning) shapeRenderer.setColor(0.9f, 0.2f, 0.2f, 1f);
        else shapeRenderer.setColor(0.2f, 0.8f, 0.3f, 1f);

        shapeRenderer.rect(timeBarX, timeBarY, timeBarWidth * Math.max(0f, Math.min(1f, timePercent)), timeBarHeight);

        if (hintEnabled) {
            shapeRenderer.setColor(0.2f, 0.55f, 0.9f, (hintsLeft > 0 && gameState == GameState.PLAYING) ? 1f : 0.35f);
            shapeRenderer.rect(hintButton.x, hintButton.y, hintButton.width, hintButton.height);
        }

        shapeRenderer.end();

        batch.begin();

        layout.setText(hudFont, "II");
        hudFont.draw(batch, "II",
            pauseButton.x + (pauseButton.width - layout.width) / 2,
            pauseButton.y + (pauseButton.height + layout.height) / 2);

        String levelText = "NIVEL " + levelNumber;
        layout.setText(hudFont, levelText);
        hudFont.draw(batch, levelText,
            Constants.WORLD_WIDTH / 2 - layout.width / 2,
            Constants.WORLD_HEIGHT - 40);

        int seconds = Math.max(0, (int) gameTime);
        String timeText = String.format("%02d:%02d", seconds / 60, seconds % 60);
        if (timerWarning) hudFont.setColor(Color.RED);
        layout.setText(hudFont, timeText);
        hudFont.draw(batch, timeText,
            Constants.WORLD_WIDTH - layout.width - 50,
            Constants.WORLD_HEIGHT - 40);
        hudFont.setColor(Color.WHITE);

        String pairsText = "Pares: " + pairsFound + "/" + totalPairs;
        layout.setText(hudFont, pairsText);
        hudFont.draw(batch, pairsText, 150, Constants.WORLD_HEIGHT - 40);

        String pcoinsText = "+" + pcoinsEarned + " " + Constants.CURRENCY_NAME;
        layout.setText(hudFont, pcoinsText);
        hudFont.draw(batch, pcoinsText, 150, Constants.WORLD_HEIGHT - 85);

        if (hintEnabled) {
            String hintText = "PISTA x" + hintsLeft;
            layout.setText(hudFont, hintText);
            hudFont.draw(batch, hintText,
                hintButton.x + (hintButton.width - layout.width) / 2f,
                hintButton.y + (hintButton.height + layout.height) / 2f);
        }

        batch.end();
    }

    private void drawPopup() {
        shapeRenderer.setProjectionMatrix(camera.combined);

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.7f);
        shapeRenderer.rect(0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);

        float popupWidth = 600f;
        float popupHeight = 500f;
        float popupX = (Constants.WORLD_WIDTH - popupWidth) / 2f;
        float popupY = (Constants.WORLD_HEIGHT - popupHeight) / 2f;

        shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
        shapeRenderer.rect(popupX, popupY, popupWidth, popupHeight);

        for (Rectangle btn : popupButtons) {
            shapeRenderer.setColor(Constants.COLOR_PRIMARY[0], Constants.COLOR_PRIMARY[1], Constants.COLOR_PRIMARY[2], 1f);
            shapeRenderer.rect(btn.x, btn.y, btn.width, btn.height);
        }

        shapeRenderer.end();

        batch.begin();

        String title;
        if (gameState == GameState.VICTORY) {
            title = "¡VICTORIA!";
            bigFont.setColor(Color.GOLD);
        } else if (gameState == GameState.DEFEAT) {
            title = "TIEMPO AGOTADO";
            bigFont.setColor(Color.RED);
        } else {
            title = "PAUSA";
            bigFont.setColor(Color.WHITE);
        }

        layout.setText(bigFont, title);
        bigFont.draw(batch, title,
            Constants.WORLD_WIDTH / 2 - layout.width / 2,
            Constants.WORLD_HEIGHT / 2 + 180);
        bigFont.setColor(Color.WHITE);

        if (gameState == GameState.VICTORY) {
            String info = "+" + pcoinsEarned + " " + Constants.CURRENCY_NAME;
            layout.setText(hudFont, info);
            hudFont.draw(batch, info,
                Constants.WORLD_WIDTH / 2 - layout.width / 2,
                Constants.WORLD_HEIGHT / 2 + 100);
        } else if (gameState == GameState.DEFEAT) {
            String info = "Pares: " + pairsFound + "/" + totalPairs;
            layout.setText(hudFont, info);
            hudFont.draw(batch, info,
                Constants.WORLD_WIDTH / 2 - layout.width / 2,
                Constants.WORLD_HEIGHT / 2 + 100);
        }

        for (int i = 0; i < popupButtons.length; i++) {
            Rectangle btn = popupButtons[i];
            layout.setText(hudFont, popupButtonTexts[i]);
            hudFont.draw(batch, popupButtonTexts[i],
                btn.x + (btn.width - layout.width) / 2,
                btn.y + (btn.height + layout.height) / 2);
        }

        batch.end();
    }

    @Override
    public void dispose() {
        if (hudFont != null) hudFont.dispose();
        if (bigFont != null) bigFont.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (gameGrid != null) gameGrid.dispose();
    }
}