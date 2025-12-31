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
 * Pantalla principal del juego
 * Muestra PCOINS, botón PLAY y navegación a otras secciones
 */
public class HomeScreen extends BaseScreen {
    
    private BitmapFont titleFont;
    private BitmapFont buttonFont;
    private BitmapFont infoFont;
    private GlyphLayout layout;
    
    private ShapeRenderer shapeRenderer;
    
    private Rectangle playButton;
    private Rectangle upgradesButton;
    private Rectangle galleryButton;
    private Rectangle settingsButton;
    
    private float playButtonPulse;
    private float pulseDirection;
    
    private Vector3 touchPos;
    
    public HomeScreen(IQWaifuMemory game) {
        super(game);
        
        titleFont = new BitmapFont();
        titleFont.getData().setScale(4f);
        titleFont.setColor(Color.WHITE);
        
        buttonFont = new BitmapFont();
        buttonFont.getData().setScale(2.5f);
        buttonFont.setColor(Color.WHITE);
        
        infoFont = new BitmapFont();
        infoFont.getData().setScale(2f);
        infoFont.setColor(Color.WHITE);
        
        layout = new GlyphLayout();
        
        shapeRenderer = new ShapeRenderer();
        touchPos = new Vector3();
        
        createButtons();
        
        playButtonPulse = 0f;
        pulseDirection = 1f;
        
        setupInput();
    }
    
    private void createButtons() {
        float centerX = Constants.WORLD_WIDTH / 2;
        
        float playSize = 300f;
        playButton = new Rectangle(
            centerX - playSize / 2,
            Constants.WORLD_HEIGHT / 2 - playSize / 2,
            playSize, playSize
        );
        
        float btnWidth = 300f;
        float btnHeight = 80f;
        float btnY = Constants.WORLD_HEIGHT * 0.2f;
        float spacing = 40f;
        
        upgradesButton = new Rectangle(
            centerX - btnWidth - spacing / 2,
            btnY,
            btnWidth, btnHeight
        );
        
        galleryButton = new Rectangle(
            centerX + spacing / 2,
            btnY,
            btnWidth, btnHeight
        );
        
        float settingsSize = 80f;
        settingsButton = new Rectangle(
            Constants.WORLD_WIDTH - settingsSize - 40,
            Constants.WORLD_HEIGHT - settingsSize - 40,
            settingsSize, settingsSize
        );
    }
    
    private void setupInput() {
        setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY, 0);
                viewport.unproject(touchPos);
                
                if (playButton.contains(touchPos.x, touchPos.y)) {
                    onPlayClicked();
                    return true;
                }
                
                if (upgradesButton.contains(touchPos.x, touchPos.y)) {
                    onUpgradesClicked();
                    return true;
                }
                
                if (galleryButton.contains(touchPos.x, touchPos.y)) {
                    onGalleryClicked();
                    return true;
                }
                
                if (settingsButton.contains(touchPos.x, touchPos.y)) {
                    onSettingsClicked();
                    return true;
                }
                
                return false;
            }
        });
    }
    
    private void onPlayClicked() {
        audioManager.playButtonClick();
        Gdx.app.log(Constants.TAG, "PLAY clicked!");
        goToScreen(new LevelSelectScreen(game));
    }
    
    private void onUpgradesClicked() {
        audioManager.playButtonClick();
        Gdx.app.log(Constants.TAG, "UPGRADES clicked!");
        goToScreen(new UpgradesScreen(game));
    }
    
    private void onGalleryClicked() {
        audioManager.playButtonClick();
        Gdx.app.log(Constants.TAG, "GALLERY clicked!");
        goToScreen(new GalleryScreen(game));
    }
    
    private void onSettingsClicked() {
        audioManager.playButtonClick();
        Gdx.app.log(Constants.TAG, "SETTINGS clicked!");
        goToScreen(new SettingsScreen(game));
    }
    
    @Override
    protected void update(float delta) {
        playButtonPulse += delta * pulseDirection * 2f;
        if (playButtonPulse >= 1f) {
            playButtonPulse = 1f;
            pulseDirection = -1f;
        } else if (playButtonPulse <= 0f) {
            playButtonPulse = 0f;
            pulseDirection = 1f;
        }
    }
    
    @Override
    protected void draw() {
        drawButtons();
        
        batch.begin();
        
        String pcoinsText = Constants.CURRENCY_NAME + ": " + getPlayerData().pcoins;
        layout.setText(infoFont, pcoinsText);
        infoFont.draw(batch, pcoinsText, 40, Constants.WORLD_HEIGHT - 40);
        
        String title = Constants.GAME_TITLE;
        layout.setText(titleFont, title);
        titleFont.draw(batch, title,
            Constants.WORLD_WIDTH / 2 - layout.width / 2,
            Constants.WORLD_HEIGHT * 0.85f);
        
        drawButtonText("PLAY", playButton);
        drawButtonText("MEJORAS", upgradesButton);
        drawButtonText("GALERIA", galleryButton);
        drawButtonText("⚙", settingsButton);
        
        batch.end();
    }
    
    private void drawButtons() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        float pulse = playButtonPulse * 10f;
        shapeRenderer.setColor(Constants.COLOR_PRIMARY[0],
                               Constants.COLOR_PRIMARY[1],
                               Constants.COLOR_PRIMARY[2], 1f);
        shapeRenderer.rect(
            playButton.x - pulse,
            playButton.y - pulse,
            playButton.width + pulse * 2,
            playButton.height + pulse * 2
        );
        
        shapeRenderer.setColor(Constants.COLOR_SECONDARY[0],
                               Constants.COLOR_SECONDARY[1],
                               Constants.COLOR_SECONDARY[2], 1f);
        shapeRenderer.rect(upgradesButton.x, upgradesButton.y,
                          upgradesButton.width, upgradesButton.height);
        
        shapeRenderer.rect(galleryButton.x, galleryButton.y,
                          galleryButton.width, galleryButton.height);
        
        shapeRenderer.setColor(0.3f, 0.3f, 0.4f, 1f);
        shapeRenderer.rect(settingsButton.x, settingsButton.y,
                          settingsButton.width, settingsButton.height);
        
        shapeRenderer.end();
    }
    
    private void drawButtonText(String text, Rectangle button) {
        layout.setText(buttonFont, text);
        buttonFont.draw(batch, text,
            button.x + (button.width - layout.width) / 2,
            button.y + (button.height + layout.height) / 2);
    }
    
    @Override
    public void show() {
        super.show();
        // Mostrar banner al entrar a la pantalla
        showBanner();
    }
    
    @Override
    public void hide() {
        super.hide();
        // Ocultar banner al salir
        hideBanner();
    }
    
    @Override
    public void dispose() {
        titleFont.dispose();
        buttonFont.dispose();
        infoFont.dispose();
        shapeRenderer.dispose();
    }
}