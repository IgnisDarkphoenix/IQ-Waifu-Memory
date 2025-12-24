package com.waifu.memory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.waifu.memory.IQWaifuMemory;
import com.waifu.memory.utils.Constants;

/**
 * Pantalla de carga inicial (Splash Screen)
 * Muestra logo, barra de carga y precarga assets esenciales
 */
public class SplashScreen extends BaseScreen {
    
    // Logo del juego
    private Texture logo;
    private boolean logoLoaded;
    
    // Fuente temporal para texto
    private BitmapFont font;
    private GlyphLayout layout;
    
    // Barra de progreso
    private ShapeRenderer shapeRenderer;
    private float loadProgress;
    private float displayProgress; // Para animación suave
    
    // Estados de carga
    private enum LoadState {
        INIT,
        LOADING_ASSETS,
        LOADING_DATA,
        COMPLETE,
        TRANSITIONING
    }
    private LoadState loadState;
    
    // Timer para transición
    private float transitionTimer;
    private static final float MIN_SPLASH_TIME = 2.0f;
    private float elapsedTime;
    
    public SplashScreen(IQWaifuMemory game) {
        super(game);
        
        // Inicializar
        font = new BitmapFont();
        font.getData().setScale(2f);
        layout = new GlyphLayout();
        
        shapeRenderer = new ShapeRenderer();
        loadProgress = 0f;
        displayProgress = 0f;
        
        loadState = LoadState.INIT;
        transitionTimer = 0f;
        elapsedTime = 0f;
        logoLoaded = false;
        
        // Intentar cargar logo
        loadLogo();
    }
    
    private void loadLogo() {
        String logoPath = Constants.PATH_UI + "logo.png";
        if (Gdx.files.internal(logoPath).exists()) {
            try {
                logo = new Texture(Gdx.files.internal(logoPath));
                logo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                logoLoaded = true;
            } catch (Exception e) {
                Gdx.app.error(Constants.TAG, "Error cargando logo");
                logoLoaded = false;
            }
        }
    }
    
    @Override
    protected void update(float delta) {
        elapsedTime += delta;
        
        switch (loadState) {
            case INIT:
                loadState = LoadState.LOADING_ASSETS;
                break;
                
            case LOADING_ASSETS:
                // Cargar assets esenciales
                assetManager.loadEssentialAssets();
                loadProgress = 0.5f;
                loadState = LoadState.LOADING_DATA;
                break;
                
            case LOADING_DATA:
                // Los datos ya se cargan en el constructor del juego
                loadProgress = 1.0f;
                loadState = LoadState.COMPLETE;
                break;
                
            case COMPLETE:
                // Esperar tiempo mínimo de splash
                if (elapsedTime >= MIN_SPLASH_TIME) {
                    loadState = LoadState.TRANSITIONING;
                }
                break;
                
            case TRANSITIONING:
                transitionTimer += delta;
                if (transitionTimer >= 0.5f) {
                    // Ir a HomeScreen
                    goToScreen(new HomeScreen(game));
                }
                break;
        }
        
        // Animar barra de progreso suavemente
        displayProgress += (loadProgress - displayProgress) * delta * 5f;
    }
    
    @Override
    protected void draw() {
        batch.begin();
        
        // Dibujar logo centrado
        float centerX = Constants.WORLD_WIDTH / 2;
        float centerY = Constants.WORLD_HEIGHT / 2 + 100;
        
        if (logoLoaded && logo != null) {
            float logoWidth = 400;
            float logoHeight = 400;
            batch.draw(logo, 
                centerX - logoWidth / 2, 
                centerY - logoHeight / 2, 
                logoWidth, logoHeight);
        } else {
            // Si no hay logo, mostrar texto
            String title = Constants.GAME_TITLE;
            layout.setText(font, title);
            font.draw(batch, title, 
                centerX - layout.width / 2, 
                centerY + layout.height / 2);
        }
        
        // Texto de carga
        String loadingText = getLoadingText();
        layout.setText(font, loadingText);
        font.draw(batch, loadingText,
            centerX - layout.width / 2,
            Constants.WORLD_HEIGHT * 0.25f);
        
        // Texto "Made with libGDX"
        font.getData().setScale(1.5f);
        String madeWith = "Made with libGDX";
        layout.setText(font, madeWith);
        font.draw(batch, madeWith,
            centerX - layout.width / 2,
            100);
        font.getData().setScale(2f);
        
        batch.end();
        
        // Dibujar barra de progreso
        drawProgressBar();
    }
    
    private void drawProgressBar() {
        float barWidth = Constants.WORLD_WIDTH * 0.7f;
        float barHeight = 30f;
        float barX = (Constants.WORLD_WIDTH - barWidth) / 2;
        float barY = Constants.WORLD_HEIGHT * 0.2f;
        
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        // Fondo de la barra
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        
        // Progreso
        shapeRenderer.setColor(Constants.COLOR_PRIMARY[0], 
                               Constants.COLOR_PRIMARY[1], 
                               Constants.COLOR_PRIMARY[2], 1f);
        shapeRenderer.rect(barX, barY, barWidth * displayProgress, barHeight);
        shapeRenderer.end();
        
        // Borde
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.end();
    }
    
    private String getLoadingText() {
        switch (loadState) {
            case INIT:
                return "Inicializando...";
            case LOADING_ASSETS:
                return "Cargando recursos...";
            case LOADING_DATA:
                return "Cargando datos...";
            case COMPLETE:
            case TRANSITIONING:
                return "¡Listo!";
            default:
                return "Cargando...";
        }
    }
    
    @Override
    public void dispose() {
        if (logo != null) {
            logo.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}