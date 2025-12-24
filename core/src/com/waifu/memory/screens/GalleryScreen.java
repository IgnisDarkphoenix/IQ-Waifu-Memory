package com.waifu.memory.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.waifu.memory.IQWaifuMemory;
import com.waifu.memory.utils.Constants;

/**
 * Pantalla de galería
 * Muestra todos los personajes y permite desbloquearlos
 */
public class GalleryScreen extends BaseScreen {
    
    // Fuentes
    private BitmapFont titleFont;
    private BitmapFont textFont;
    private GlyphLayout layout;
    
    // Renderizado
    private ShapeRenderer shapeRenderer;
    
    // Grid de personajes
    private static final int COLUMNS = 4;
    private static final int VISIBLE_ROWS = 5;
    private Rectangle[] characterSlots;
    
    // Scroll
    private float scrollY;
    private float maxScrollY;
    
    // Botones
    private Rectangle backButton;
    
    // Visor de personaje
    private boolean viewingCharacter;
    private int viewedCharacterId;
    private int viewedVariant;
    private Rectangle closeViewerButton;
    private Rectangle prevVariantButton;
    private Rectangle nextVariantButton;
    private Rectangle unlockButton;
    
    // Input
    private Vector3 touchPos;
    
    public GalleryScreen(IQWaifuMemory game) {
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
        
        scrollY = 0;
        viewingCharacter = false;
        
        createUI();
        setupInput();
    }
    
    private void createUI() {
        float padding = 40f;
        
        // Botón atrás
        backButton = new Rectangle(padding, Constants.WORLD_HEIGHT - 100, 80, 80);
        
        // Grid de personajes
        float slotSize = (Constants.WORLD_WIDTH - padding * 2 - (COLUMNS - 1) * 20) / COLUMNS;
        int totalRows = (int) Math.ceil((float) Constants.TOTAL_CHARACTERS / COLUMNS);
        
        characterSlots = new Rectangle[Constants.TOTAL_CHARACTERS];
        
        float startY = Constants.WORLD_HEIGHT - 180;
        
        for (int i = 0; i < Constants.TOTAL_CHARACTERS; i++) {
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            
            float x = padding + col * (slotSize + 20);
            float y = startY - row * (slotSize + 20);
            
            characterSlots[i] = new Rectangle(x, y, slotSize, slotSize);
        }
        
        // Calcular scroll máximo
        float contentHeight = totalRows * (slotSize + 20);
        float visibleHeight = VISIBLE_ROWS * (slotSize + 20);
        maxScrollY = Math.max(0, contentHeight - visibleHeight);
        
        // Botones del visor
        closeViewerButton = new Rectangle(Constants.WORLD_WIDTH - 120, Constants.WORLD_HEIGHT - 100, 80, 80);
        prevVariantButton = new Rectangle(40, Constants.WORLD_HEIGHT / 2 - 40, 80, 80);
        nextVariantButton = new Rectangle(Constants.WORLD_WIDTH - 120, Constants.WORLD_HEIGHT / 2 - 40, 80, 80);
        unlockButton = new Rectangle(
            Constants.WORLD_WIDTH / 2 - 150,
            100,
            300, 80
        );
    }
    
    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY, 0);
                viewport.unproject(touchPos);
                
                if (viewingCharacter) {
                    return handleViewerInput();
                }
                
                if (backButton.contains(touchPos.x, touchPos.y)) {
                    audioManager.playButtonClick();
                    goToScreen(new HomeScreen(game));
                    return true;
                }
                
                // Check character slots
                for (int i = 0; i < characterSlots.length; i++) {
                    Rectangle slot = characterSlots[i];
                    float adjustedY = slot.y + scrollY;
                    
                    if (touchPos.x >= slot.x && touchPos.x <= slot.x + slot.width &&
                        touchPos.y >= adjustedY && touchPos.y <= adjustedY + slot.height) {
                        
                        openCharacterViewer(i);
                        return true;
                    }
                }
                
                return false;
            }
            
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (!viewingCharacter) {
                    float deltaY = Gdx.input.getDeltaY() * 2f;
                    scrollY = Math.max(0, Math.min(maxScrollY, scrollY + deltaY));
                }
                return true;
            }
        });
    }
    
    private boolean handleViewerInput() {
        if (closeViewerButton.contains(touchPos.x, touchPos.y)) {
            audioManager.playButtonClick();
            closeCharacterViewer();
            return true;
        }
        
        if (prevVariantButton.contains(touchPos.x, touchPos.y)) {
            audioManager.playButtonClick();
            changeVariant(-1);
            return true;
        }
        
        if (nextVariantButton.contains(touchPos.x, touchPos.y)) {
            audioManager.playButtonClick();
            changeVariant(1);
            return true;
        }
        
        if (unlockButton.contains(touchPos.x, touchPos.y)) {
            tryUnlock();
            return true;
        }
        
        return false;
    }
    
    private void openCharacterViewer(int characterId) {
        audioManager.playButtonClick();
        viewingCharacter = true;
        viewedCharacterId = characterId;
        viewedVariant = 0;
    }
    
    private void closeCharacterViewer() {
        viewingCharacter = false;
        // Descargar texturas de variantes para liberar memoria
        for (int i = 0; i < 4; i++) {
            assetManager.unloadCharacterTexture(viewedCharacterId, i);
        }
    }
    
    private void changeVariant(int direction) {
        int unlockLevel = getPlayerData().getCharacterUnlockLevel(viewedCharacterId);
        int maxVariant = Math.min(unlockLevel, 3);
        
        viewedVariant += direction;
        if (viewedVariant < 0) viewedVariant = maxVariant;
        if (viewedVariant > maxVariant) viewedVariant = 0;
    }
    
    private void tryUnlock() {
        if (getPlayerData().unlockCharacterLevel(viewedCharacterId)) {
            audioManager.playButtonClick();
            audioManager.playCoinCollect();
            saveProgress();
        }
    }
    
    @Override
    protected void update(float delta) {
        // Sin actualizaciones
    }
    
    @Override
    protected void draw() {
        if (viewingCharacter) {
            drawViewer();
        } else {
            drawGallery();
        }
    }
    
    private void drawGallery() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Botón atrás
        shapeRenderer.setColor(0.3f, 0.3f, 0.4f, 1f);
        shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);
        
        // Slots de personajes
        for (int i = 0; i < characterSlots.length; i++) {
            Rectangle slot = characterSlots[i];
            float adjustedY = slot.y + scrollY;
            
            // Solo dibujar si está visible
            if (adjustedY > -slot.height && adjustedY < Constants.WORLD_HEIGHT - 100) {
                int unlockLevel = getPlayerData().getCharacterUnlockLevel(i);
                
                if (unlockLevel > 0) {
                    // Desbloqueado - color según nivel
                    float brightness = 0.3f + (unlockLevel * 0.175f);
                    shapeRenderer.setColor(brightness, brightness * 0.8f, brightness * 1.2f, 1f);
                } else {
                    // Bloqueado
                    shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
                }
                
                shapeRenderer.rect(slot.x, adjustedY, slot.width, slot.height);
            }
        }
        
        shapeRenderer.end();
        
        // Texto
        batch.begin();
        
        // Título
        String title = "GALERIA";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, Constants.WORLD_WIDTH / 2 - layout.width / 2, Constants.WORLD_HEIGHT - 50);
        
        // PCOINS
        String pcoins = Constants.CURRENCY_NAME + ": " + getPlayerData().pcoins;
        layout.setText(textFont, pcoins);
        textFont.draw(batch, pcoins, Constants.WORLD_WIDTH - layout.width - 40, Constants.WORLD_HEIGHT - 60);
        
        // Progreso
        String progress = String.format("%.1f%%", getPlayerData().getGalleryCompletionPercent());
        layout.setText(textFont, progress);
        textFont.draw(batch, progress, 150, Constants.WORLD_HEIGHT - 60);
        
        // Botón atrás
        layout.setText(textFont, "<");
        textFont.draw(batch, "<",
            backButton.x + (backButton.width - layout.width) / 2,
            backButton.y + (backButton.height + layout.height) / 2);
        
        // Indicadores en slots
        for (int i = 0; i < characterSlots.length; i++) {
            Rectangle slot = characterSlots[i];
            float adjustedY = slot.y + scrollY;
            
            if (adjustedY > -slot.height && adjustedY < Constants.WORLD_HEIGHT - 100) {
                int unlockLevel = getPlayerData().getCharacterUnlockLevel(i);
                
                String indicator;
                if (unlockLevel == 0) {
                    indicator = "🔒";
                } else {
                    // Mostrar estrellas según nivel
                    indicator = getStarsString(unlockLevel);
                }
                
                layout.setText(textFont, indicator);
                textFont.draw(batch, indicator,
                    slot.x + (