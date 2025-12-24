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

/**
 * Pantalla de mejoras
 * Permite mejorar valor de par y tiempo de juego
 */
public class UpgradesScreen extends BaseScreen {
    
    // Fuentes
    private BitmapFont titleFont;
    private BitmapFont textFont;
    private GlyphLayout layout;
    
    // Renderizado
    private ShapeRenderer shapeRenderer;
    
    // Botones
    private Rectangle backButton;
    private Rectangle upgradePairButton;
    private Rectangle upgradeTimeButton;
    
    // Input
    private Vector3 touchPos;
    
    public UpgradesScreen(IQWaifuMemory game) {
        super(game);
        
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);
        titleFont.setColor(Color.WHITE);
        
        textFont = new BitmapFont();
        textFont.getData().setScale(2f);
        textFont.setColor(Color.WHITE);
        
        layout = new GlyphLayout();
        shapeRenderer = new ShapeRenderer();
        touchPos = new Vector3();
        
        createUI();
        setupInput();
    }
    
    private void createUI() {
        float padding = 40f;
        float buttonWidth = 300f;
        float buttonHeight = 80f;
        
        // Botón atrás
        backButton = new Rectangle(padding, Constants.WORLD_HEIGHT - 100, 80, 80);
        
        // Botón mejorar valor de par
        upgradePairButton = new Rectangle(
            Constants.WORLD_WIDTH / 2 - buttonWidth / 2,
            Constants.WORLD_HEIGHT / 2 + 50,
            buttonWidth, buttonHeight
        );
        
        // Botón mejorar tiempo
        upgradeTimeButton = new Rectangle(
            Constants.WORLD_WIDTH / 2 - buttonWidth / 2,
            Constants.WORLD_HEIGHT / 2 - 200,
            buttonWidth, buttonHeight
        );
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
                
                if (upgradePairButton.contains(touchPos.x, touchPos.y)) {
                    tryUpgradePair();
                    return true;
                }
                
                if (upgradeTimeButton.contains(touchPos.x, touchPos.y)) {
                    tryUpgradeTime();
                    return true;
                }
                
                return false;
            }
        });
    }
    
    private void tryUpgradePair() {
        if (getPlayerData().upgradePairValue()) {
            audioManager.playButtonClick();
            audioManager.playCoinCollect();
            saveProgress();
            Gdx.app.log(Constants.TAG, "Valor de par mejorado a nivel " + getPlayerData().pairValueLevel);
        }
    }
    
    private void tryUpgradeTime() {
        if (getPlayerData().upgradeTime()) {
            audioManager.playButtonClick();
            audioManager.playCoinCollect();
            saveProgress();
            Gdx.app.log(Constants.TAG, "Tiempo mejorado a nivel " + getPlayerData().timeBonusLevel);
        }
    }
    
    @Override
    protected void update(float delta) {
        // Sin actualizaciones necesarias
    }
    
    @Override
    protected void draw() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Botón atrás
        shapeRenderer.setColor(0.3f, 0.3f, 0.4f, 1f);
        shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);
        
        // Panel de mejora de valor de par
        shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
        shapeRenderer.rect(60, Constants.WORLD_HEIGHT / 2, Constants.WORLD_WIDTH - 120, 200);
        
        // Botón mejorar par
        boolean canUpgradePair = getPlayerData().canUpgradePairValue();
        if (canUpgradePair) {
            shapeRenderer.setColor(Constants.COLOR_PRIMARY[0], Constants.COLOR_PRIMARY[1], Constants.COLOR_PRIMARY[2], 1f);
        } else {
            shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        }
        shapeRenderer.rect(upgradePairButton.x, upgradePairButton.y, upgradePairButton.width, upgradePairButton.height);
        
        // Panel de mejora de tiempo
        shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
        shapeRenderer.rect(60, Constants.WORLD_HEIGHT / 2 - 250, Constants.WORLD_WIDTH - 120, 200);
        
        // Botón mejorar tiempo
        boolean canUpgradeTime = getPlayerData().canUpgradeTime();
        if (canUpgradeTime) {
            shapeRenderer.setColor(Constants.COLOR_PRIMARY[0], Constants.COLOR_PRIMARY[1], Constants.COLOR_PRIMARY[2], 1f);
        } else {
            shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        }
        shapeRenderer.rect(upgradeTimeButton.x, upgradeTimeButton.y, upgradeTimeButton.width, upgradeTimeButton.height);
        
        shapeRenderer.end();
        
        // Texto
        batch.begin();
        
        // Título
        String title = "MEJORAS";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, Constants.WORLD_WIDTH / 2 - layout.width / 2, Constants.WORLD_HEIGHT - 50);
        
        // PCOINS
        String pcoins = Constants.CURRENCY_NAME + ": " + getPlayerData().pcoins;
        layout.setText(textFont, pcoins);
        textFont.draw(batch, pcoins, Constants.WORLD_WIDTH - layout.width - 40, Constants.WORLD_HEIGHT - 60);
        
        // Botón atrás
        layout.setText(textFont, "<");
        textFont.draw(batch, "<",
            backButton.x + (backButton.width - layout.width) / 2,
            backButton.y + (backButton.height + layout.height) / 2);
        
        // Info mejora de par
        int pairLevel = getPlayerData().pairValueLevel;
        String pairTitle = "VALOR DE PAR";
        String pairInfo = "Nivel: " + pairLevel + "/" + Constants.MAX_PAIR_LEVEL;
        String pairValue = "Actual: +" + getPlayerData().getCurrentPairValue() + " PCOINS/par";
        
        textFont.draw(batch, pairTitle, 100, Constants.WORLD_HEIGHT / 2 + 180);
        textFont.draw(batch, pairInfo, 100, Constants.WORLD_HEIGHT / 2 + 140);
        textFont.draw(batch, pairValue, 100, Constants.WORLD_HEIGHT / 2 + 100);
        
        // Botón de mejora par
        String pairBtnText;
        if (pairLevel >= Constants.MAX_PAIR_LEVEL) {
            pairBtnText = "MAXIMO";
        } else {
            pairBtnText = "MEJORAR: " + getPlayerData().getNextPairUpgradeCost();
        }
        layout.setText(textFont, pairBtnText);
        textFont.draw(batch, pairBtnText,
            upgradePairButton.x + (upgradePairButton.width - layout.width) / 2,
            upgradePairButton.y + (upgradePairButton.height + layout.height) / 2);
        
        // Info mejora de tiempo
        int timeLevel = getPlayerData().timeBonusLevel;
        String timeTitle = "TIEMPO DE JUEGO";
        String timeInfo = "Nivel: " + timeLevel + "/" + Constants.MAX_TIME_LEVEL;
        String timeValue = "Actual: " + getPlayerData().getCurrentBaseTime() + " segundos base";
        
        textFont.draw(batch, timeTitle, 100, Constants.WORLD_HEIGHT / 2 - 70);
        textFont.draw(batch, timeInfo, 100, Constants.WORLD_HEIGHT / 2 - 110);
        textFont.draw(batch, timeValue, 100, Constants.WORLD_HEIGHT / 2 - 150);
        
        // Botón de mejora tiempo
        String timeBtnText;
        if (timeLevel >= Constants.MAX_TIME_LEVEL) {
            timeBtnText = "MAXIMO";
        } else {
            timeBtnText = "MEJORAR: " + getPlayerData().getNextTimeUpgradeCost();
        }
        layout.setText(textFont, timeBtnText);
        textFont.draw(batch, timeBtnText,
            upgradeTimeButton.x + (upgradeTimeButton.width - layout.width) / 2,
            upgradeTimeButton.y + (upgradeTimeButton.height + layout.height) / 2);
        
        batch.end();
    }
    
    @Override
    public void dispose() {
        titleFont.dispose();
        textFont.dispose();
        shapeRenderer.dispose();
    }
}