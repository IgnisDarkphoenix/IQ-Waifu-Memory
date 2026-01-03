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
 * Pantalla de configuración
 * Permite ajustar volumen de música, efectos y ver estadísticas
 */
public class SettingsScreen extends BaseScreen {
    
    // Fuentes
    private BitmapFont titleFont;
    private BitmapFont textFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    // Renderizado
    private ShapeRenderer shapeRenderer;
    
    // Botones
    private Rectangle backButton;
    
    // Sliders de volumen
    private Rectangle musicSliderBg;
    private Rectangle musicSliderFill;
    private Rectangle sfxSliderBg;
    private Rectangle sfxSliderFill;
    
    // Botones de acción
    private Rectangle resetButton;
    
    // Estado de arrastre
    private boolean draggingMusic;
    private boolean draggingSfx;
    
    // Input
    private Vector3 touchPos;
    
    public SettingsScreen(IQWaifuMemory game) {
        super(game);
        
        // Inicializar fuentes
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);
        titleFont.setColor(Color.WHITE);
        
        textFont = new BitmapFont();
        textFont.getData().setScale(2f);
        textFont.setColor(Color.WHITE);
        
        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.5f);
        smallFont.setColor(Color.LIGHT_GRAY);
        
        layout = new GlyphLayout();
        shapeRenderer = new ShapeRenderer();
        touchPos = new Vector3();
        
        draggingMusic = false;
        draggingSfx = false;
        
        createUI();
        setupInput();
    }
    
    private void createUI() {
        float padding = 40f;
        float sliderWidth = Constants.WORLD_WIDTH - 200;
        float sliderHeight = 40f;
        float sliderX = 100f;
        
        // Botón atrás
        backButton = new Rectangle(padding, Constants.WORLD_HEIGHT - 100, 80, 80);
        
        // Slider de música
        float musicY = Constants.WORLD_HEIGHT - 350;
        musicSliderBg = new Rectangle(sliderX, musicY, sliderWidth, sliderHeight);
        musicSliderFill = new Rectangle(sliderX, musicY, 
            sliderWidth * getPlayerData().musicVolume, sliderHeight);
        
        // Slider de efectos
        float sfxY = Constants.WORLD_HEIGHT - 500;
        sfxSliderBg = new Rectangle(sliderX, sfxY, sliderWidth, sliderHeight);
        sfxSliderFill = new Rectangle(sliderX, sfxY, 
            sliderWidth * getPlayerData().sfxVolume, sliderHeight);
        
        // Botón de reset (para desarrollo/testing)
        resetButton = new Rectangle(
            Constants.WORLD_WIDTH / 2 - 150,
            150,
            300, 80
        );
    }
    
    private void setupInput() {
        // FIX: Usar setInputProcessor seguro
        setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY, 0);
                viewport.unproject(touchPos);
                
                // Verificar si toca los sliders
                if (isInSliderArea(musicSliderBg, touchPos.x, touchPos.y)) {
                    draggingMusic = true;
                    updateMusicVolume(touchPos.x);
                    return true;
                }
                
                if (isInSliderArea(sfxSliderBg, touchPos.x, touchPos.y)) {
                    draggingSfx = true;
                    updateSfxVolume(touchPos.x);
                    return true;
                }
                
                return true;
            }
            
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY, 0);
                viewport.unproject(touchPos);
                
                if (!draggingMusic && !draggingSfx) {
                    // Procesar como click
                    if (backButton.contains(touchPos.x, touchPos.y)) {
                        audioManager.playButtonClick();
                        saveProgress();
                        goToScreen(new HomeScreen(game));
                        return true;
                    }
                    
                    if (resetButton.contains(touchPos.x, touchPos.y)) {
                        audioManager.playButtonClick();
                        resetAllData();
                        return true;
                    }
                }
                
                draggingMusic = false;
                draggingSfx = false;
                saveProgress();
                
                return true;
            }
            
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                touchPos.set(screenX, screenY, 0);
                viewport.unproject(touchPos);
                
                if (draggingMusic) {
                    updateMusicVolume(touchPos.x);
                    return true;
                }
                
                if (draggingSfx) {
                    updateSfxVolume(touchPos.x);
                    return true;
                }
                
                return false;
            }
        });
    }
    
    private boolean isInSliderArea(Rectangle slider, float x, float y) {
        // Área expandida para facilitar el toque
        float expandedHeight = 60f;
        return x >= slider.x && x <= slider.x + slider.width &&
               y >= slider.y - 10 && y <= slider.y + expandedHeight;
    }
    
    private void updateMusicVolume(float touchX) {
        float volume = (touchX - musicSliderBg.x) / musicSliderBg.width;
        volume = Math.max(0, Math.min(1, volume));
        
        getPlayerData().musicVolume = volume;
        audioManager.setMusicVolume(volume);
        
        // Actualizar visual del slider
        musicSliderFill.width = musicSliderBg.width * volume;
    }
    
    private void updateSfxVolume(float touchX) {
        float volume = (touchX - sfxSliderBg.x) / sfxSliderBg.width;
        volume = Math.max(0, Math.min(1, volume));
        
        getPlayerData().sfxVolume = volume;
        audioManager.setSfxVolume(volume);
        
        // Actualizar visual del slider
        sfxSliderFill.width = sfxSliderBg.width * volume;
        
        // Reproducir sonido de prueba
        audioManager.playButtonClick();
    }
    
    private void resetAllData() {
        // ADVERTENCIA: Esto borra todo el progreso
        // FIX: Acceder correctamente al SaveManager
        game.getSaveManager().resetAll();
        Gdx.app.log(Constants.TAG, "Todos los datos han sido reseteados");
        
        // Volver a home con datos frescos
        goToScreen(new HomeScreen(game));
    }
    
    @Override
    protected void update(float delta) {
        // Actualizar sliders visuales
        musicSliderFill.width = musicSliderBg.width * getPlayerData().musicVolume;
        sfxSliderFill.width = sfxSliderBg.width * getPlayerData().sfxVolume;
    }
    
    @Override
    protected void draw() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Barra superior
        shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 1f);
        shapeRenderer.rect(0, Constants.WORLD_HEIGHT - 130, Constants.WORLD_WIDTH, 130);
        
        // Botón atrás
        shapeRenderer.setColor(0.3f, 0.3f, 0.4f, 1f);
        shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);
        
        // ===== SLIDER DE MÚSICA =====
        // Fondo del slider
        shapeRenderer.setColor(0.2f, 0.2f, 0.25f, 1f);
        shapeRenderer.rect(musicSliderBg.x, musicSliderBg.y, 
                          musicSliderBg.width, musicSliderBg.height);
        
        // Relleno del slider
        shapeRenderer.setColor(Constants.COLOR_PRIMARY[0], 
                               Constants.COLOR_PRIMARY[1], 
                               Constants.COLOR_PRIMARY[2], 1f);
        shapeRenderer.rect(musicSliderFill.x, musicSliderFill.y, 
                          musicSliderFill.width, musicSliderFill.height);
        
        // ===== SLIDER DE EFECTOS =====
        // Fondo del slider
        shapeRenderer.setColor(0.2f, 0.2f, 0.25f, 1f);
        shapeRenderer.rect(sfxSliderBg.x, sfxSliderBg.y, 
                          sfxSliderBg.width, sfxSliderBg.height);
        
        // Relleno del slider
        shapeRenderer.setColor(Constants.COLOR_SECONDARY[0], 
                               Constants.COLOR_SECONDARY[1], 
                               Constants.COLOR_SECONDARY[2], 1f);
        shapeRenderer.rect(sfxSliderFill.x, sfxSliderFill.y, 
                          sfxSliderFill.width, sfxSliderFill.height);
        
        // ===== PANEL DE ESTADÍSTICAS =====
        float statsY = Constants.WORLD_HEIGHT - 650;
        shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 1f);
        shapeRenderer.rect(40, statsY, Constants.WORLD_WIDTH - 80, 200);
        
        // ===== BOTÓN RESET =====
        shapeRenderer.setColor(0.6f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(resetButton.x, resetButton.y, 
                          resetButton.width, resetButton.height);
        
        shapeRenderer.end();
        
        // ===== TEXTO =====
        batch.begin();
        
        // Título
        String title = "AJUSTES";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, 
            Constants.WORLD_WIDTH / 2 - layout.width / 2, 
            Constants.WORLD_HEIGHT - 45);
        
        // Botón atrás
        layout.setText(textFont, "<");
        textFont.draw(batch, "<",
            backButton.x + (backButton.width - layout.width) / 2,
            backButton.y + (backButton.height + layout.height) / 2);
        
        // ===== MÚSICA =====
        String musicLabel = "Musica";
        layout.setText(textFont, musicLabel);
        textFont.draw(batch, musicLabel, 100, musicSliderBg.y + 100);
        
        int musicPercent = (int)(getPlayerData().musicVolume * 100);
        String musicValue = musicPercent + "%";
        layout.setText(textFont, musicValue);
        textFont.draw(batch, musicValue, 
            Constants.WORLD_WIDTH - layout.width - 100, 
            musicSliderBg.y + 100);
        
        // ===== EFECTOS =====
        String sfxLabel = "Efectos";
        layout.setText(textFont, sfxLabel);
        textFont.draw(batch, sfxLabel, 100, sfxSliderBg.y + 100);
        
        int sfxPercent = (int)(getPlayerData().sfxVolume * 100);
        String sfxValue = sfxPercent + "%";
        layout.setText(textFont, sfxValue);
        textFont.draw(batch, sfxValue, 
            Constants.WORLD_WIDTH - layout.width - 100, 
            sfxSliderBg.y + 100);
        
        // ===== ESTADÍSTICAS =====
        float statsY2 = Constants.WORLD_HEIGHT - 480;
        
        String statsTitle = "ESTADISTICAS";
        layout.setText(textFont, statsTitle);
        textFont.draw(batch, statsTitle, 
            Constants.WORLD_WIDTH / 2 - layout.width / 2, 
            statsY2);
        
        // Datos estadísticos
        smallFont.setColor(Color.LIGHT_GRAY);
        
        String stat1 = "Partidas jugadas: " + getPlayerData().totalGamesPlayed;
        smallFont.draw(batch, stat1, 60, statsY2 - 50);
        
        String stat2 = "Victorias: " + getPlayerData().totalVictories;
        smallFont.draw(batch, stat2, 60, statsY2 - 90);
        
        String stat3 = "Derrotas: " + getPlayerData().totalDefeats;
        smallFont.draw(batch, stat3, 60, statsY2 - 130);
        
        String stat4 = "Pares encontrados: " + getPlayerData().totalPairsFound;
        smallFont.draw(batch, stat4, Constants.WORLD_WIDTH / 2, statsY2 - 50);
        
        String stat5 = "PCOINS ganados: " + getPlayerData().totalPcoinsEarned;
        smallFont.draw(batch, stat5, Constants.WORLD_WIDTH / 2, statsY2 - 90);
        
        String stat6 = "Mejor racha: " + getPlayerData().bestWinStreak;
        smallFont.draw(batch, stat6, Constants.WORLD_WIDTH / 2, statsY2 - 130);
        
        smallFont.setColor(Color.WHITE);
        
        // ===== BOTÓN RESET =====
        String resetText = "BORRAR DATOS";
        layout.setText(textFont, resetText);
        textFont.draw(batch, resetText,
            resetButton.x + (resetButton.width - layout.width) / 2,
            resetButton.y + (resetButton.height + layout.height) / 2);
        
        // Advertencia
        smallFont.setColor(Color.RED);
        String warning = "(¡Esto borrara todo el progreso!)";
        layout.setText(smallFont, warning);
        smallFont.draw(batch, warning,
            Constants.WORLD_WIDTH / 2 - layout.width / 2,
            resetButton.y - 20);
        smallFont.setColor(Color.WHITE);
        
        // Versión
        String version = "Version " + Constants.VERSION;
        layout.setText(smallFont, version);
        smallFont.draw(batch, version,
            Constants.WORLD_WIDTH / 2 - layout.width / 2,
            40);
        
        batch.end();
    }
    
    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (textFont != null) textFont.dispose();
        if (smallFont != null) smallFont.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}