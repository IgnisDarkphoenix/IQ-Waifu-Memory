package com.waifu.memory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.waifu.memory.IQWaifuMemory;
import com.waifu.memory.data.PlayerData;
import com.waifu.memory.managers.AudioManager;
import com.waifu.memory.utils.Constants;

/**
 * Clase base para todas las pantallas del juego
 * FIXED: Memory leak de InputProcessor
 */
public abstract class BaseScreen implements Screen {
    
    protected final IQWaifuMemory game;
    protected final SpriteBatch batch;
    protected final OrthographicCamera camera;
    protected final Viewport viewport;
    
    protected final com.waifu.memory.managers.AssetManager assetManager;
    protected final AudioManager audioManager;
    
    // FIX: Mantener referencia al InputProcessor para limpiarlo
    protected InputProcessor inputProcessor;
    
    private static final float TRANSITION_DURATION = Constants.TRANSITION_DURATION;
    private float transitionAlpha = 0f;
    private boolean transitionIn = true;
    private BaseScreen nextScreen = null;
    
    public BaseScreen(IQWaifuMemory game) {
        this.game = game;
        this.batch = game.getBatch();
        this.assetManager = game.getAssetManager();
        this.audioManager = game.getAudioManager();
        
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        this.camera.position.set(Constants.WORLD_WIDTH / 2, Constants.WORLD_HEIGHT / 2, 0);
        this.camera.update();
        
        this.inputProcessor = null;
    }
    
    /**
     * FIX: Método helper para establecer el InputProcessor de forma segura
     * Las subclases deben llamar a este método en lugar de Gdx.input.setInputProcessor
     */
    protected void setInputProcessor(InputProcessor processor) {
        this.inputProcessor = processor;
        Gdx.input.setInputProcessor(processor);
    }
    
    /**
     * FIX: Limpia el InputProcessor actual
     */
    private void clearInputProcessor() {
        if (inputProcessor != null) {
            if (Gdx.input.getInputProcessor() == inputProcessor) {
                Gdx.input.setInputProcessor(null);
            }
            inputProcessor = null;
        }
    }
    
    /**
     * Muestra el banner de ads (si está disponible)
     */
    protected void showBanner() {
        if (game.hasAdHandler()) {
            game.getAdHandler().showBanner();
        }
    }
    
    /**
     * Oculta el banner de ads
     */
    protected void hideBanner() {
        if (game.hasAdHandler()) {
            game.getAdHandler().hideBanner();
        }
    }
    
    /**
     * Verifica si el banner está visible
     */
    protected boolean isBannerVisible() {
        return game.hasAdHandler() && game.getAdHandler().isBannerVisible();
    }
    
    protected PlayerData getPlayerData() {
        return game.getPlayerData();
    }
    
    protected void saveProgress() {
        game.savePlayerData();
    }
    
    protected void goToScreen(BaseScreen screen) {
        nextScreen = screen;
        transitionIn = false;
        transitionAlpha = 0f;
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(
            Constants.COLOR_BACKGROUND[0],
            Constants.COLOR_BACKGROUND[1],
            Constants.COLOR_BACKGROUND[2],
            Constants.COLOR_BACKGROUND[3]
        );
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        if (transitionIn || nextScreen == null) {
            update(delta);
        }
        
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        
        draw();
        
        updateTransition(delta);
    }
    
    private void updateTransition(float delta) {
        if (transitionIn) {
            transitionAlpha += delta / TRANSITION_DURATION;
            if (transitionAlpha >= 1f) {
                transitionAlpha = 1f;
                transitionIn = false;
            }
        } else if (nextScreen != null) {
            transitionAlpha += delta / TRANSITION_DURATION;
            if (transitionAlpha >= 1f) {
                game.setScreen(nextScreen);
                dispose();
            }
        }
    }
    
    protected abstract void update(float delta);
    protected abstract void draw();
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(Constants.WORLD_WIDTH / 2, Constants.WORLD_HEIGHT / 2, 0);
    }
    
    @Override
    public void show() {
        transitionIn = true;
        transitionAlpha = 0f;
    }
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void hide() {
        // FIX: Limpiar el InputProcessor cuando la pantalla se oculta
        clearInputProcessor();
    }
    
    @Override
    public void dispose() {
        // FIX: Asegurarse de limpiar el InputProcessor al dispose
        clearInputProcessor();
    }
}