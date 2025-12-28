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
import com.waifu.memory.utils.Constants;

public abstract class BaseScreen implements Screen {

    protected final IQWaifuMemory game;
    protected final SpriteBatch batch;
    protected final OrthographicCamera camera;
    protected final Viewport viewport;
    protected final AssetManager assetManager;
    protected final AudioManager audioManager;

    public BaseScreen(IQWaifuMemory game) {
        this.game = game;
        this.batch = game.getBatch();
        this.assetManager = game.getAssetManager();
        this.audioManager = game.getAudioManager();

        camera = new OrthographicCamera();
        viewport = new FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT, camera);
        camera.position.set(Constants.WORLD_WIDTH / 2f, Constants.WORLD_HEIGHT / 2f, 0);
        camera.update();
    }

    protected PlayerData getPlayerData() {
        return game.getPlayerData();
    }

    protected void saveProgress() {
        game.savePlayerData();
    }

    protected void goToScreen(BaseScreen newScreen) {
        // Limpiar input processor antes de cambiar
        Gdx.input.setInputProcessor(null);
        game.setScreen(newScreen);
    }

    @Override
    public void show() {
        // Subclases pueden override
    }

    @Override
    public void render(float delta) {
        // Limpiar pantalla
        Gdx.gl.glClearColor(
            Constants.COLOR_BACKGROUND[0],
            Constants.COLOR_BACKGROUND[1],
            Constants.COLOR_BACKGROUND[2],
            Constants.COLOR_BACKGROUND[3]
        );
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Habilitar blending globalmente
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        update(delta);
        draw();
    }

    protected abstract void update(float delta);
    protected abstract void draw();

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(Constants.WORLD_WIDTH / 2f, Constants.WORLD_HEIGHT / 2f, 0);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        // Limpiar input al ocultar pantalla
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {}
}