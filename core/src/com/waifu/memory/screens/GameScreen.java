package com.waifu.memory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.waifu.memory.IQWaifuMemory;
import com.waifu.memory.entities.Card;
import com.waifu.memory.entities.GameGrid;
import com.waifu.memory.utils.Constants;

/**
 * Pantalla principal de juego
 * Maneja la lógica del memory match
 */
public class GameScreen extends BaseScreen {
    
    // Nivel actual
    private int levelNumber;
    private int gridSize;
    private int totalPairs;
    
    // Estado del juego
    private enum GameState {
        PLAYING,
        CHECKING_MATCH,
        VICTORY,
        DEFEAT,
        PAUSED
    }
    private GameState gameState;
    
    // Grid de cartas
    private GameGrid gameGrid;
    
    // Cartas seleccionadas
    private Card firstCard;
    private Card secondCard;
    private float checkTimer;
    
    // Timer del juego
    private float gameTime;
    private float maxTime;
    private boolean timerWarning;
    
    // Puntuación
    private int pairsFound;
    private int pcoinsEarned;
    private int cardsFlippedSinceShuffle;
    
    // Modificadores
    private boolean shuffleEnabled;
    private boolean multiGridEnabled;
    
    // UI
    private BitmapFont hudFont;
    private BitmapFont bigFont;
    private GlyphLayout layout;
    private ShapeRenderer shapeRenderer;
    
    // Botones
    private Rectangle pauseButton;
    
    // Popup
    private boolean showingPopup;
    private Rectangle[] popupButtons;
    private String[] popupButtonTexts;
    
    // Input
    private Vector3 touchPos;
    
    public GameScreen(IQWaifuMemory game, int levelNumber) {
        super(game);
        
        this.levelNumber = levelNumber;
        this.gameState = GameState.PLAYING;
        
        // Determinar configuración según nivel
        setupLevel();
        
        // Inicializar UI
        hudFont = new BitmapFont();
        hudFont.getData().setScale(2.5f);
        hudFont.setColor(Color.WHITE);
        
        bigFont = new BitmapFont();
        bigFont.getData().setScale(4f);
        bigFont.setColor(Color.WHITE);
        
        layout = new GlyphLayout();
        shapeRenderer = new ShapeRenderer();
        touchPos = new Vector3();
        
        // Crear grid
        gameGrid = new GameGrid(gridSize, this);
        
        // Inicializar variables
        firstCard = null;
        secondCard = null;
        checkTimer = 0;
        pairsFound = 0;
        pcoinsEarned = 0;
        cardsFlippedSinceShuffle = 0;
        showingPopup = false;
        timerWarning = false;
        
        // Botón de pausa
        pauseButton = new Rectangle(40, Constants.WORLD_HEIGHT - 100, 80, 80);
        
        setupInput();
        
        Gdx.app.log(Constants.TAG, "Iniciando nivel " + levelNumber + 
                   " - Grid: " + gridSize + "x" + gridSize + 
                   " - Tiempo: " + maxTime + "s");
    }
    
    private void setupLevel() {
        // Determinar dificultad y grid según nivel
        if (levelNumber <= Constants.LEVELS_EASY_END) {
            gridSize = Constants.GRID_EASY;
            shuffleEnabled = false;
            multiGridEnabled = false;
            maxTime = getPlayerData().getCurrentBaseTime() + Constants.TIME_BONUS_4X4;
        } else if (levelNumber <= Constants.LEVELS_NORMAL_END) {
            gridSize = Constants.GRID_NORMAL;
            shuffleEnabled = levelNumber >= 45; // Activar shuffle desde nivel 45
            multiGridEnabled = false;
            maxTime = getPlayerData().getCurrentBaseTime() + Constants.TIME_BONUS_6X6;
        } else {
            gridSize = Constants.GRID_HARD;
            shuffleEnabled = true;
            multiGridEnabled = levelNumber >= 78; // Multi-grid desde nivel 78
            maxTime = getPlayerData().getCurrentBaseTime() + Constants.TIME_BONUS_8X8;
        }
        
        totalPairs = (gridSize * gridSize) / 2;
        gameTime = maxTime;
    }
    
    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY, 0);
                viewport.unproject(touchPos);
                
                // Si hay popup, manejar sus botones
                if (showingPopup) {
                    handlePopupInput();
                    return true;
                }
                
                // Botón de pausa
                if (pauseButton.contains(touchPos.x, touchPos.y)) {
                    togglePause();
                    return true;
                }
                
                // Solo procesar clicks en cartas si estamos jugando
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
        if (clickedCard == firstCard) return;
        
        // Voltear carta
        clickedCard.flip();
        audioManager.playCardFlip();
        cardsFlippedSinceShuffle++;
        
        if (firstCard == null) {
            // Primera carta
            firstCard = clickedCard;
        } else {
            // Segunda carta
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
                case 0: // Ver Ad x2
                    showRewardedAdForDouble();
                    break;
                case 1: // Mejoras
                    goToScreen(new UpgradesScreen(game));
                    break;
                case 2: // Siguiente nivel
                    goToScreen(new GameScreen(game, levelNumber + 1));
                    break;
                case 3: // Home
                    goToScreen(new HomeScreen(game));
                    break;
            }
        } else if (gameState == GameState.DEFEAT) {
            switch (buttonIndex) {
                case 0: // Ver Ad +15s
                    showRewardedAdForTime();
                    break;
                case 1: // Reintentar
                    goToScreen(new GameScreen(game, levelNumber));
                    break;
                case 2: // Home
                    goToScreen(new HomeScreen(game));
                    break;
            }
        } else if (gameState == GameState.PAUSED) {
            switch (buttonIndex) {
                case 0: // Continuar
                    togglePause();
                    break;
                case 1: // Reiniciar
                    goToScreen(new GameScreen(game, levelNumber));
                    break;
                case 2: // Salir
                    goToScreen(new HomeScreen(game));
                    break;
            }
        }
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
        if (game.hasAdHandler() && game.getAdHandler().isRewardedAdLoaded()) {
            game.getAdHandler().showRewardedAd(new IQWaifuMemory.RewardCallback() {
                @Override
                public void onRewardEarned() {
                    // Duplicar ganancias
                    int bonus = pcoinsEarned;
                    pcoinsEarned *= 2;
                    getPlayerData().addPcoins(bonus);
                    saveProgress();
                }
                
                @Override
                public void onAdFailed() {
                    Gdx.app.log(Constants.TAG, "Ad failed to show");
                }
            });
        }
    }
    
    private void showRewardedAdForTime() {
        if (game.hasAdHandler() && game.getAdHandler().isRewardedAdLoaded()) {
            game.getAdHandler().showRewardedAd(new IQWaifuMemory.RewardCallback() {
                @Override
                public void onRewardEarned() {
                    // Añadir tiempo extra y continuar
                    gameTime = Constants.AD_EXTRA_TIME;
                    gameState = GameState.PLAYING;
                    showingPopup = false;
                }
                
                @Override
                public void onAdFailed() {
                    Gdx.app.log(Constants.TAG, "Ad failed to show");
                }
            });
        }
    }
    
    @Override
    protected void update(float delta) {
        if (gameState == GameState.PLAYING) {
            // Actualizar timer
            gameTime -= delta;
            
            // Warning cuando quedan 10 segundos
            if (gameTime <= 10 && !timerWarning) {
                timerWarning = true;
                audioManager.playTimerWarning();
            }
            
            // Verificar derrota por tiempo
            if (gameTime <= 0) {
                gameTime = 0;
                onDefeat();
                return;
            }
            
            // Actualizar animaciones de cartas
            gameGrid.update(delta);
            
        } else if (gameState == GameState.CHECKING_MATCH) {
            // Esperar antes de verificar match
            checkTimer -= delta;
            
            if (checkTimer <= 0) {
                checkMatch();
            }
        }
    }
    
    private void checkMatch() {
        if (firstCard.getCharacterId() == secondCard.getCharacterId()) {
            // ¡Match!
            firstCard.setMatched(true);
            secondCard.setMatched(true);
            pairsFound++;
            
            // Calcular PCOINS
            int pairValue = getPlayerData().getCurrentPairValue();
            pcoinsEarned += pairValue;
            getPlayerData().addPcoins(pairValue);
            
            audioManager.playMatch();
            audioManager.playCoinCollect();
            
            // Verificar victoria
            if (pairsFound >= totalPairs) {
                onVictory();
                return;
            }
        } else {
            // No match
            firstCard.flipBack();
            secondCard.flipBack();
            audioManager.playNoMatch();
        }
        
        // Shuffle si está habilitado
        if (shuffleEnabled && cardsFlippedSinceShuffle >= Constants.SHUFFLE_INTERVAL) {
            gameGrid.shuffleUnmatched();
            cardsFlippedSinceShuffle = 0;
        }
        
        // Reset selección
        firstCard = null;
        secondCard = null;
        gameState = GameState.PLAYING;
    }
    
    private void onVictory() {
        gameState = GameState.VICTORY;
        audioManager.playVictory();
        
        // Bonus por tiempo restante
        int timeBonus = (int) (gameTime / 2);
        pcoinsEarned += timeBonus;
        getPlayerData().addPcoins(timeBonus);
        
        // Aplicar multiplicador de dificultad
        float multiplier = getDifficultyMultiplier();
        if (multiplier > 1) {
            int bonusMultiplier = (int) (pcoinsEarned * (multiplier - 1));
            pcoinsEarned += bonusMultiplier;
            getPlayerData().addPcoins(bonusMultiplier);
        }
        
        // Actualizar progreso
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
        
        getPlayerData().recordGamePlayed(false, pairsFound);
        saveProgress();
        
        showDefeatPopup();
    }
    
    private float getDifficultyMultiplier() {
        if (levelNumber <= Constants.LEVELS_EASY_END) {
            return Constants.MULTIPLIER_EASY;
        } else if (levelNumber <= Constants.LEVELS_NORMAL_END) {
            return Constants.MULTIPLIER_NORMAL;
        } else {
            return Constants.MULTIPLIER_HARD;
        }
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
                buttonWidth, buttonHeight
            );
        }
    }
    
    @Override
    protected void draw() {
        // Dibujar grid de cartas
        gameGrid.draw(batch, shapeRenderer, camera);
        
        // Dibujar HUD
        drawHUD();
        
        // Dibujar popup si está activo
        if (showingPopup) {
            drawPopup();
        }
    }
    
    private void drawHUD() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Barra superior
        shapeRenderer.setColor(0.15f, 0.15f, 0.25f, 0.9f);
        shapeRenderer.rect(0, Constants.WORLD_HEIGHT - 120, Constants.WORLD_WIDTH, 120);
        
        // Botón pausa
        shapeRenderer.setColor(0.3f, 0.3f, 0.4f, 1f);
        shapeRenderer.rect(pauseButton.x, pauseButton.y, pauseButton.width, pauseButton.height);
        
        // Barra de tiempo
        float timeBarWidth = 400f;
        float timeBarHeight = 30f;
        float timeBarX = Constants.WORLD_WIDTH - timeBarWidth - 40;
        float timeBarY = Constants.WORLD_HEIGHT - 80;
        float timePercent = gameTime / maxTime;
        
        // Fondo de barra
        shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
        shapeRenderer.rect(timeBarX, timeBarY, timeBarWidth, timeBarHeight);
        
        // Progreso de tiempo
        if (timerWarning) {
            shapeRenderer.setColor(0.9f, 0.2f, 0.2f, 1f);
        } else {
            shapeRenderer.setColor(0.2f, 0.8f, 0.3f, 1f);
        }
        shapeRenderer.rect(timeBarX, timeBarY, timeBarWidth * 