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
import com.waifu.memory.utils.Constants;

public class LevelSelectScreen extends BaseScreen {

    private BitmapFont titleFont;
    private BitmapFont levelFont;
    private BitmapFont tabFont;
    private GlyphLayout layout;

    private ShapeRenderer shapeRenderer;

    private Rectangle tabEasy;
    private Rectangle tabNormal;
    private Rectangle tabHard;
    private int selectedTab;

    private Rectangle[] levelButtons;
    private static final int LEVELS_PER_ROW = 5;
    private static final int VISIBLE_ROWS = 6;

    private float scrollY;
    private float maxScrollY;

    private Rectangle backButton;

    private Vector3 touchPos;

    public LevelSelectScreen(IQWaifuMemory game) {
        super(game);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);
        titleFont.setColor(Color.WHITE);

        levelFont = new BitmapFont();
        levelFont.getData().setScale(2f);
        levelFont.setColor(Color.WHITE);

        tabFont = new BitmapFont();
        tabFont.getData().setScale(2f);
        tabFont.setColor(Color.WHITE);

        layout = new GlyphLayout();
        shapeRenderer = new ShapeRenderer();
        touchPos = new Vector3();

        selectedTab = 0;
        scrollY = 0;

        createUI();
        setupInput();
    }

    private void createUI() {
        float padding = 40f;

        backButton = new Rectangle(padding, Constants.WORLD_HEIGHT - 100, 80, 80);

        float tabWidth = (Constants.WORLD_WIDTH - padding * 4) / 3;
        float tabHeight = 80f;
        float tabY = Constants.WORLD_HEIGHT - 200;

        tabEasy = new Rectangle(padding, tabY, tabWidth, tabHeight);
        tabNormal = new Rectangle(padding * 2 + tabWidth, tabY, tabWidth, tabHeight);
        tabHard = new Rectangle(padding * 3 + tabWidth * 2, tabY, tabWidth, tabHeight);

        createLevelButtons();
    }

    private void createLevelButtons() {
        int totalLevels = getLevelsForTab(selectedTab);
        levelButtons = new Rectangle[totalLevels];

        float startY = Constants.WORLD_HEIGHT - 320;
        float buttonSize = 150f;
        float spacing = 20f;
        float startX = (Constants.WORLD_WIDTH - (LEVELS_PER_ROW * buttonSize + (LEVELS_PER_ROW - 1) * spacing)) / 2;

        for (int i = 0; i < totalLevels; i++) {
            int row = i / LEVELS_PER_ROW;
            int col = i % LEVELS_PER_ROW;

            float x = startX + col * (buttonSize + spacing);
            float y = startY - row * (buttonSize + spacing);

            levelButtons[i] = new Rectangle(x, y, buttonSize, buttonSize);
        }

        int totalRows = (int) Math.ceil((float) totalLevels / LEVELS_PER_ROW);
        float contentHeight = totalRows * (150f + 20f);
        float visibleHeight = VISIBLE_ROWS * (150f + 20f);
        maxScrollY = Math.max(0, contentHeight - visibleHeight);
    }

    private int getLevelsForTab(int tab) {
        switch (tab) {
            case 0: return Constants.LEVELS_EASY_END - Constants.LEVELS_EASY_START + 1;
            case 1: return Constants.LEVELS_NORMAL_END - Constants.LEVELS_NORMAL_START + 1;
            case 2: return Constants.LEVELS_HARD_END - Constants.LEVELS_HARD_START + 1;
            default: return 33;
        }
    }

    private int getLevelOffset(int tab) {
        switch (tab) {
            case 0: return Constants.LEVELS_EASY_START - 1;
            case 1: return Constants.LEVELS_NORMAL_START - 1;
            case 2: return Constants.LEVELS_HARD_START - 1;
            default: return 0;
        }
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY, 0);
                viewport.unproject(touchPos);

                if (backButton.contains(touchPos.x, touchPos.y)) {
                    audioManager.playButtonClick();
                    goToScreen(new HomeScreen(game));
                    return true;
                }

                if (tabEasy.contains(touchPos.x, touchPos.y)) {
                    selectTab(0);
                    return true;
                }
                if (tabNormal.contains(touchPos.x, touchPos.y)) {
                    if (getPlayerData().maxLevelCompleted >= Constants.LEVELS_EASY_END) {
                        selectTab(1);
                    }
                    return true;
                }
                if (tabHard.contains(touchPos.x, touchPos.y)) {
                    if (getPlayerData().maxLevelCompleted >= Constants.LEVELS_NORMAL_END) {
                        selectTab(2);
                    }
                    return true;
                }

                for (int i = 0; i < levelButtons.length; i++) {
                    Rectangle btn = levelButtons[i];
                    float adjustedY = btn.y + scrollY;
                    if (touchPos.x >= btn.x && touchPos.x <= btn.x + btn.width &&
                        touchPos.y >= adjustedY && touchPos.y <= adjustedY + btn.height) {

                        int levelNum = getLevelOffset(selectedTab) + i + 1;
                        if (isLevelUnlocked(levelNum)) {
                            startLevel(levelNum);
                        }
                        return true;
                    }
                }

                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                float deltaY = Gdx.input.getDeltaY() * 2f;
                scrollY = Math.max(0, Math.min(maxScrollY, scrollY + deltaY));
                return true;
            }
        });
    }

    private void selectTab(int tab) {
        if (tab != selectedTab) {
            audioManager.playButtonClick();
            selectedTab = tab;
            scrollY = 0;
            createLevelButtons();
        }
    }

    private boolean isLevelUnlocked(int levelNum) {
        if (levelNum == 1) return true;
        return getPlayerData().maxLevelCompleted >= levelNum - 1;
    }

    private void startLevel(int levelNum) {
        audioManager.playButtonClick();
        goToScreen(new GameScreen(game, levelNum));
    }

    @Override
    protected void update(float delta) {
    }

    @Override
    protected void draw() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.3f, 0.3f, 0.4f, 1f);
        shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);

        drawTab(tabEasy, 0, true);
        drawTab(tabNormal, 1, getPlayerData().maxLevelCompleted >= Constants.LEVELS_EASY_END);
        drawTab(tabHard, 2, getPlayerData().maxLevelCompleted >= Constants.LEVELS_NORMAL_END);

        for (int i = 0; i < levelButtons.length; i++) {
            Rectangle btn = levelButtons[i];
            int levelNum = getLevelOffset(selectedTab) + i + 1;
            float adjustedY = btn.y + scrollY;

            if (adjustedY > -btn.height && adjustedY < Constants.WORLD_HEIGHT - 200) {
                if (isLevelUnlocked(levelNum)) {
                    if (getPlayerData().maxLevelCompleted >= levelNum) {
                        shapeRenderer.setColor(0.2f, 0.7f, 0.3f, 1f);
                    } else {
                        shapeRenderer.setColor(Constants.COLOR_PRIMARY[0], Constants.COLOR_PRIMARY[1], Constants.COLOR_PRIMARY[2], 1f);
                    }
                } else {
                    shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
                }

                shapeRenderer.rect(btn.x, adjustedY, btn.width, btn.height);
            }
        }

        shapeRenderer.end();

        batch.begin();

        String title = "SELECCIONAR NIVEL";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, Constants.WORLD_WIDTH / 2 - layout.width / 2, Constants.WORLD_HEIGHT - 40);

        String pcoins = Constants.CURRENCY_NAME + ": " + getPlayerData().pcoins;
        layout.setText(levelFont, pcoins);
        levelFont.draw(batch, pcoins, Constants.WORLD_WIDTH - layout.width - 40, Constants.WORLD_HEIGHT - 60);

        layout.setText(levelFont, "<");
        levelFont.draw(batch, "<",
            backButton.x + (backButton.width - layout.width) / 2,
            backButton.y + (backButton.height + layout.height) / 2);

        drawTabText("FACIL", tabEasy);
        drawTabText("NORMAL", tabNormal);
        drawTabText("DIFICIL", tabHard);

        for (int i = 0; i < levelButtons.length; i++) {
            Rectangle btn = levelButtons[i];
            int levelNum = getLevelOffset(selectedTab) + i + 1;
            float adjustedY = btn.y + scrollY;

            if (adjustedY > -btn.height && adjustedY < Constants.WORLD_HEIGHT - 200) {
                String levelText = isLevelUnlocked(levelNum) ? String.valueOf(levelNum) : "LOCK";
                layout.setText(levelFont, levelText);
                levelFont.draw(batch, levelText,
                    btn.x + (btn.width - layout.width) / 2,
                    adjustedY + (btn.height + layout.height) / 2);
            }
        }

        batch.end();
    }

    private void drawTab(Rectangle tab, int tabIndex, boolean unlocked) {
        if (selectedTab == tabIndex) {
            shapeRenderer.setColor(Constants.COLOR_PRIMARY[0], Constants.COLOR_PRIMARY[1], Constants.COLOR_PRIMARY[2], 1f);
        } else if (unlocked) {
            shapeRenderer.setColor(0.4f, 0.4f, 0.5f, 1f);
        } else {
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        }
        shapeRenderer.rect(tab.x, tab.y, tab.width, tab.height);
    }

    private void drawTabText(String text, Rectangle tab) {
        layout.setText(tabFont, text);
        tabFont.draw(batch, text, tab.x + (tab.width - layout.width) / 2, tab.y + (tab.height + layout.height) / 2);
    }

    @Override
    public void dispose() {
        titleFont.dispose();
        levelFont.dispose();
        tabFont.dispose();
        shapeRenderer.dispose();
    }
}