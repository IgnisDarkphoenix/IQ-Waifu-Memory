package com.waifu.memory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.waifu.memory.IQWaifuMemory;
import com.waifu.memory.data.PlayerData;
import com.waifu.memory.managers.AssetManager;
import com.waifu.memory.managers.AudioManager;
import com.waifu.memory.managers.SaveManager;
import com.waifu.memory.utils.Constants;

/**
 * Clase base para todas las pantallas del juego
 * Proporciona acceso común a managers y métodos de utilidad
 */
public abstract class BaseScreen implements Screen {
    
    protected IQWaifuMemory game;
    protected SpriteBatch batch;
    
    // Cámara y viewport para UI
    protected OrthographicCamera camera;
    protected Viewport viewport;
    
    // Acceso rápido a managers
    protected AssetManager assetManager;
    protected AudioManager audioManager;
    protected SaveManager saveManager;
    
    // Color de fondo
    protected float bgRed = Constants.COLOR_BACKGROUND[0];
    protected float bgGreen = Constants.COLOR_BACKGROUND[1];
    protected float bgBlue = Constants.COLOR_BACKGROUND[2];
    protected float bgAlpha = Constants.COLOR_BACKGROUND[3];
    
    public BaseScreen(IQWaifuMemory game) {
        this.game = game;
        this.batch = game.getBatch();
        this.assetManager = game.getAssetManager();
        this.audioManager = game.getAudioManager();
        this.saveManager = game.getSaveManager();
        
        // Configurar cámara y viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        camera.position.set(Constants.WORLD_WIDTH / 2, Constants.WORLD_HEIGHT / 2, 0);
        camera.update();
    }
    
    /**
     * Obtiene los datos del jugador
     */
    protected PlayerData getPlayerData() {
        return saveManager.getPlayerData();
    }
    
    /**
     * Guarda el progreso del jugador
     */
    protected void saveProgress() {
        saveManager.save();
    }
    
    /**
     * Limpia la pantalla con el color de fondo
     */
    protected void clearScreen() {
        Gdx.gl.glClearColor(bgRed, bgGreen, bgBlue, bgAlpha);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
    
    /**
     * Actualiza la lógica del juego
     * @param delta Tiempo desde el último frame
     */
    protected abstract void update(float delta);
    
    /**
     * Dibuja los elementos en pantalla
     */
    protected abstract void draw();
    
    @Override
    public void render(float delta) {
        // Actualizar lógica
        update(delta);
        
        // Limpiar pantalla
        clearScreen();
        
        // Aplicar cámara al batch
        batch.setProjectionMatrix(camera.combined);
        
        // Dibujar
        draw();
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(Constants.WORLD_WIDTH / 2, Constants.WORLD_HEIGHT / 2, 0);
        camera.update();
    }
    
    @Override
    public void show() {
        Gdx.app.log(Constants.TAG, "Mostrando: " + this.getClass().getSimpleName());
    }
    
    @Override
    public void hide() {
        Gdx.app.log(Constants.TAG, "Ocultando: " + this.getClass().getSimpleName());
    }
    
    @Override
    public void pause() {
        // Guardar al pausar
        saveProgress();
    }
    
    @Override
    public void resume() {
        // Subclases pueden sobrescribir
    }
    
    @Override
    public void dispose() {
        // Subclases deben sobrescribir si tienen recursos propios
    }
    
    /**
     * Cambia a otra pantalla
     */
    protected void goToScreen(Screen screen) {
        game.setScreen(screen);
    }
    
    /**
     * Convierte coordenadas de pantalla a coordenadas del mundo
     */
    protected float screenToWorldX(float screenX) {
        return screenX * Constants.WORLD_WIDTH / Gdx.graphics.getWidth();
    }
    
    protected float screenToWorldY(float screenY) {
        return Constants.WORLD_HEIGHT - (screenY * Constants.WORLD_HEIGHT / Gdx.graphics.getHeight());
    }
}