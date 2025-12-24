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
    private BitmapFont smallFont;
    private GlyphLayout layout;
    
    // Renderizado
    private ShapeRenderer shapeRenderer;
    
    // Grid de personajes
    private static final int COLUMNS = 4;
    private static final int VISIBLE_ROWS = 5;
    private Rectangle[] characterSlots;
    private float slotSize;
    
    // Scroll
    private float scrollY;
    private float maxScrollY;
    private float lastTouchY;
    private boolean isDragging;
    
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
        
        // Inicializar fuentes
        titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);
        titleFont.setColor(Color.WHITE);
        
        textFont = new BitmapFont();
        textFont.getData().setScale(2f);
        textFont.setColor(Color.WHITE);
        
        smallFont = new BitmapFont();
        smallFont.getData().setScale(1.5f);
        smallFont.setColor(Color.WHITE);
        
        layout = new GlyphLayout();
        shapeRenderer = new ShapeRenderer();
        touchPos = new Vector3();
        
        scrollY = 0;
        lastTouchY = 0;
        isDragging = false;
        viewingCharacter = false;
        viewedCharacterId = 0;
        viewedVariant = 0;
        
        createUI();
        setupInput();
    }
    
    private void createUI() {
        float padding = 40f;
        float spacing = 20f;
        
        // Botón atrás
        backButton = new Rectangle(padding, Constants.WORLD_HEIGHT - 100, 80, 80);
        
        // Calcular tamaño de slots
        slotSize = (Constants.WORLD_WIDTH - padding * 2 - (COLUMNS - 1) * spacing) / COLUMNS;
        int totalRows = (int) Math.ceil((float) Constants.TOTAL_CHARACTERS / COLUMNS);
        
        characterSlots = new Rectangle[Constants.TOTAL_CHARACTERS];
        
        float startY = Constants.WORLD_HEIGHT - 200;
        
        for (int i = 0; i < Constants.TOTAL_CHARACTERS; i++) {
            int row = i / COLUMNS;
            int col = i % COLUMNS;
            
            float x = padding + col * (slotSize + spacing);
            float y = startY - row * (slotSize + spacing);
            
            characterSlots[i] = new Rectangle(x, y, slotSize, slotSize);
        }
        
        // Calcular scroll máximo
        float contentHeight = totalRows * (slotSize + spacing);
        float visibleHeight = Constants.WORLD_HEIGHT - 250;
        maxScrollY = Math.max(0, contentHeight - visibleHeight);
        
        // Botones del visor de personaje
        float btnSize = 80f;
        closeViewerButton = new Rectangle(
            Constants.WORLD_WIDTH - btnSize - 40,
            Constants.WORLD_HEIGHT - btnSize - 40,
            btnSize, btnSize
        );
        
        prevVariantButton = new Rectangle(
            40,
            Constants.WORLD_HEIGHT / 2 - btnSize / 2,
            btnSize, btnSize
        );
        
        nextVariantButton = new Rectangle(
            Constants.WORLD_WIDTH - btnSize - 40,
            Constants.WORLD_HEIGHT / 2 - btnSize / 2,
            btnSize, btnSize
        );
        
        unlockButton = new Rectangle(
            Constants.WORLD_WIDTH / 2 - 150,
            150,
            300, 80
        );
    }
    
    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY, 0);
                viewport.unproject(touchPos);
                lastTouchY = touchPos.y;
                isDragging = false;
                return true;
            }
            
            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY, 0);
                viewport.unproject(touchPos);
                
                if (!isDragging) {
                    if (viewingCharacter) {
                        handleViewerClick();
                    } else {
                        handleGalleryClick();
                    }
                }
                
                isDragging = false;
                return true;
            }
            
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (viewingCharacter) return false;
                
                touchPos.set(screenX, screenY, 0);
                viewport.unproject(touchPos);
                
                float deltaY = lastTouchY - touchPos.y;
                
                if (Math.abs(deltaY) > 5) {
                    isDragging = true;
                    scrollY = Math.max(0, Math.min(maxScrollY, scrollY + deltaY));
                }
                
                lastTouchY = touchPos.y;
                return true;
            }
        });
    }
    
    private void handleGalleryClick() {
        if (backButton.contains(touchPos.x, touchPos.y)) {
            audioManager.playButtonClick();
            goToScreen(new HomeScreen(game));
            return;
        }
        
        for (int i = 0; i < characterSlots.length; i++) {
            Rectangle slot = characterSlots[i];
            float adjustedY = slot.y + scrollY;
            
            if (touchPos.x >= slot.x && touchPos.x <= slot.x + slot.width &&
                touchPos.y >= adjustedY && touchPos.y <= adjustedY + slot.height) {
                openCharacterViewer(i);
                return;
            }
        }
    }
    
    private void handleViewerClick() {
        if (closeViewerButton.contains(touchPos.x, touchPos.y)) {
            audioManager.playButtonClick();
            closeCharacterViewer();
            return;
        }
        
        if (prevVariantButton.contains(touchPos.x, touchPos.y)) {
            audioManager.playButtonClick();
            changeVariant(-1);
            return;
        }
        
        if (nextVariantButton.contains(touchPos.x, touchPos.y)) {
            audioManager.playButtonClick();
            changeVariant(1);
            return;
        }
        
        if (unlockButton.contains(touchPos.x, touchPos.y)) {
            tryUnlockCurrentVariant();
            return;
        }
    }
    
    private void openCharacterViewer(int characterId) {
        audioManager.playButtonClick();
        viewingCharacter = true;
        viewedCharacterId = characterId;
        viewedVariant = 0;
        Gdx.app.log(Constants.TAG, "Abriendo visor para personaje: " + characterId);
    }
    
    private void closeCharacterViewer() {
        viewingCharacter = false;
        
        for (int i = 0; i < Constants.ARTS_PER_CHARACTER; i++) {
            assetManager.unloadCharacterTexture(viewedCharacterId, i);
        }
        
        Gdx.app.log(Constants.TAG, "Cerrando visor de personaje");
    }
    
    private void changeVariant(int direction) {
        int unlockLevel = getPlayerData().getCharacterUnlockLevel(viewedCharacterId);
        int maxViewableVariant = Math.max(0, unlockLevel - 1);
        
        viewedVariant += direction;
        
        if (viewedVariant < 0) {
            viewedVariant = maxViewableVariant;
        } else if (viewedVariant > maxViewableVariant) {
            viewedVariant = 0;
        }
    }
    
    private void tryUnlockCurrentVariant() {
        int currentUnlockLevel = getPlayerData().getCharacterUnlockLevel(viewedCharacterId);
        
        if (currentUnlockLevel >= Constants.ARTS_PER_CHARACTER) {
            return;
        }
        
        if (getPlayerData().unlockCharacterLevel(viewedCharacterId)) {
            audioManager.playButtonClick();
            audioManager.playCoinCollect();
            saveProgress();
            Gdx.app.log(Constants.TAG, "Desbloqueado nivel " +
                       (currentUnlockLevel + 1) + " del personaje " + viewedCharacterId);
        }
    }
    
    private int getNextUnlockCost(int characterId) {
        int currentLevel = getPlayerData().getCharacterUnlockLevel(characterId);
        
        switch (currentLevel) {
            case 0: return Constants.GALLERY_BASE_COST;
            case 1: return Constants.GALLERY_STAR1_COST;
            case 2: return Constants.GALLERY_STAR2_COST;
            case 3: return Constants.GALLERY_STAR3_COST;
            default: return -1;
        }
    }
    
    private String getStarsString(int unlockLevel) {
        switch (unlockLevel) {
            case 1: return "BASE";
            case 2: return "★";
            case 3: return "★★";
            case 4: return "★★★";
            default: return "";
        }
    }
    
    private String getVariantName(int variant) {
        switch (variant) {
            case 0: return "Base";
            case 1: return "★ Variante 1";
            case 2: return "★★ Variante 2";
            case 3: return "★★★ Variante 3";
            default: return "Desconocido";
        }
    }
    
    @Override
    protected void update(float delta) {
        // Animaciones si son necesarias
    }
    
    @Override
    protected void draw() {
        if (viewingCharacter) {
            drawCharacterViewer();
        } else {
            drawGalleryGrid();
        }
    }
    
    private void drawGalleryGrid() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Barra superior
        shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 1f);
        shapeRenderer.rect(0, Constants.WORLD_HEIGHT - 130, Constants.WORLD_WIDTH, 130);
        
        // Botón atrás
        shapeRenderer.setColor(0.3f, 0.3f, 0.4f, 1f);
        shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);
        
        // Slots de personajes
        for (int i = 0; i < characterSlots.length; i++) {
            Rectangle slot = characterSlots[i];
            float adjustedY = slot.y + scrollY;
            
            if (adjustedY > -slot.height && adjustedY < Constants.WORLD_HEIGHT - 130) {
                int unlockLevel = getPlayerData().getCharacterUnlockLevel(i);
                
                if (unlockLevel > 0) {
                    float r = 0.2f + (unlockLevel * 0.1f);
                    float g = 0.15f + (unlockLevel * 0.15f);
                    float b = 0.3f + (unlockLevel * 0.1f);
                    shapeRenderer.setColor(r, g, b, 1f);
                } else {
                    shapeRenderer.setColor(0.15f, 0.15f, 0.18f, 1f);
                }
                
                shapeRenderer.rect(slot.x, adjustedY, slot.width, slot.height);
            }
        }
        
        shapeRenderer.end();
        
        // Bordes
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.4f, 0.4f, 0.5f, 1f);
        
        for (int i = 0; i < characterSlots.length; i++) {
            Rectangle slot = characterSlots[i];
            float adjustedY = slot.y + scrollY;
            
            if (adjustedY > -slot.height && adjustedY < Constants.WORLD_HEIGHT - 130) {
                shapeRenderer.rect(slot.x, adjustedY, slot.width, slot.height);
            }
        }
        
        shapeRenderer.end();
        
        // Texto
        batch.begin();
        
        // Título
        String title = "GALERIA";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title,
            Constants.WORLD_WIDTH / 2 - layout.width / 2,
            Constants.WORLD_HEIGHT - 45);
        
        // PCOINS
        String pcoinsText = Constants.CURRENCY_NAME + ": " + getPlayerData().pcoins;
        layout.setText(textFont, pcoinsText);
        textFont.draw(batch, pcoinsText,
            Constants.WORLD_WIDTH - layout.width - 40,
            Constants.WORLD_HEIGHT - 50);
        
        // Progreso
        float completion = getPlayerData().getGalleryCompletionPercent();
        String progressText = String.format("Completado: %.1f%%", completion);
        layout.setText(smallFont, progressText);
        smallFont.draw(batch, progressText,
            Constants.WORLD_WIDTH / 2 - layout.width / 2,
            Constants.WORLD_HEIGHT - 100);
        
        // Botón atrás
        layout.setText(textFont, "<");
        textFont.draw(batch, "<",
            backButton.x + (backButton.width - layout.width) / 2,
            backButton.y + (backButton.height + layout.height) / 2);
        
        // Indicadores en slots
        for (int i = 0; i < characterSlots.length; i++) {
            Rectangle slot = characterSlots[i];
            float adjustedY = slot.y + scrollY;
            
            if (adjustedY > -slot.height && adjustedY < Constants.WORLD_HEIGHT - 130) {
                int unlockLevel = getPlayerData().getCharacterUnlockLevel(i);
                
                // Número del personaje
                String numText = String.format("#%02d", i + 1);
                layout.setText(smallFont, numText);
                smallFont.setColor(Color.WHITE);
                smallFont.draw(batch, numText, slot.x + 5, adjustedY + slot.height - 5);
                
                // Estado
                String statusText;
                if (unlockLevel == 0) {
                    statusText = "BLOQUEADO";
                    smallFont.setColor(Color.GRAY);
                } else if (unlockLevel >= Constants.ARTS_PER_CHARACTER) {
                    statusText = "COMPLETO";
                    smallFont.setColor(Color.GOLD);
                } else {
                    statusText = getStarsString(unlockLevel);
                    smallFont.setColor(Color.WHITE);
                }
                
                layout.setText(smallFont, statusText);
                smallFont.draw(batch, statusText,
                    slot.x + (slot.width - layout.width) / 2,
                    adjustedY + layout.height + 10);
                
                smallFont.setColor(Color.WHITE);
                
                // Costo
                if (unlockLevel < Constants.ARTS_PER_CHARACTER) {
                    int cost = getNextUnlockCost(i);
                    String costText = cost + " P";
                    layout.setText(smallFont, costText);
                    
                    if (getPlayerData().pcoins >= cost) {
                        smallFont.setColor(Color.GREEN);
                    } else {
                        smallFont.setColor(Color.RED);
                    }
                    
                    smallFont.draw(batch, costText,
                        slot.x + (slot.width - layout.width) / 2,
                        adjustedY + slot.height / 2);
                    
                    smallFont.setColor(Color.WHITE);
                }
            }
        }
        
        batch.end();
    }
    
    private void drawCharacterViewer() {
        int unlockLevel = getPlayerData().getCharacterUnlockLevel(viewedCharacterId);
        boolean isCurrentVariantUnlocked = viewedVariant < unlockLevel;
        
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Fondo
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 1f);
        shapeRenderer.rect(0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);
        
        // Panel central
        float panelSize = Constants.WORLD_WIDTH - 160;
        float panelX = (Constants.WORLD_WIDTH - panelSize) / 2;
        float panelY = Constants.WORLD_HEIGHT / 2 - panelSize / 2 + 50;
        
        shapeRenderer.setColor(0.2f, 0.2f, 0.25f, 1f);
        shapeRenderer.rect(panelX, panelY, panelSize, panelSize);
        
        // Botón cerrar
        shapeRenderer.setColor(0.6f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(closeViewerButton.x, closeViewerButton.y,
                          closeViewerButton.width, closeViewerButton.height);
        
        // Botones de navegación
        if (unlockLevel > 1) {
            shapeRenderer.setColor(0.3f, 0.3f, 0.5f, 1f);
            shapeRenderer.rect(prevVariantButton.x, prevVariantButton.y,
                              prevVariantButton.width, prevVariantButton.height);
            shapeRenderer.rect(nextVariantButton.x, nextVariantButton.y,
                              nextVariantButton.width, nextVariantButton.height);
        }
        
        // Botón desbloqueo
        if (unlockLevel < Constants.ARTS_PER_CHARACTER) {
            int cost = getNextUnlockCost(viewedCharacterId);
            boolean canAfford = getPlayerData().pcoins >= cost;
            
            if (canAfford) {
                shapeRenderer.setColor(Constants.COLOR_PRIMARY[0],
                                       Constants.COLOR_PRIMARY[1],
                                       Constants.COLOR_PRIMARY[2], 1f);
            } else {
                shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
            }
            
            shapeRenderer.rect(unlockButton.x, unlockButton.y,
                              unlockButton.width, unlockButton.height);
        }
        
        shapeRenderer.end();
        
        // Dibujar imagen y marco
        batch.begin();
        
        if (isCurrentVariantUnlocked) {
            Texture charTexture = assetManager.getCharacterTexture(viewedCharacterId, viewedVariant);
            Texture frameTexture = assetManager.getFrameTexture(viewedVariant);
            
            float imgSize = panelSize - 40;
            float imgX = panelX + 20;
            float imgY = panelY + 20;
            
            if (charTexture != null) {
                // 1. Dibujar personaje
                batch.draw(charTexture, imgX, imgY, imgSize, imgSize);
                
                // 2. Dibujar marco encima
                if (frameTexture != null) {
                    batch.draw(frameTexture, imgX, imgY, imgSize, imgSize);
                }
            } else {
                String placeholder = "Imagen no\ndisponible";
                layout.setText(textFont, placeholder);
                textFont.draw(batch, placeholder,
                    Constants.WORLD_WIDTH / 2 - layout.width / 2,
                    Constants.WORLD_HEIGHT / 2 + layout.height / 2);
            }
        } else {
            String lockedText = "BLOQUEADO";
            layout.setText(titleFont, lockedText);
            titleFont.setColor(Color.GRAY);
            titleFont.draw(batch, lockedText,
                Constants.WORLD_WIDTH / 2 - layout.width / 2,
                Constants.WORLD_HEIGHT / 2);
            titleFont.setColor(Color.WHITE);
        }
        
        // Nombre del personaje
        String charName = "Personaje #" + (viewedCharacterId + 1);
        layout.setText(titleFont, charName);
        titleFont.draw(batch, charName,
            Constants.WORLD_WIDTH / 2 - layout.width / 2,
            Constants.WORLD_HEIGHT - 60);
        
        // Variante actual
        String variantText = getVariantName(viewedVariant);
        layout.setText(textFont, variantText);
        textFont.draw(batch, variantText,
            Constants.WORLD_WIDTH / 2 - layout.width / 2,
            Constants.WORLD_HEIGHT - 110);
        
        // Indicador
        int maxViewable = Math.max(1, unlockLevel);
        String indicator = (viewedVariant + 1) + "/" + maxViewable;
        layout.setText(textFont, indicator);
        textFont.draw(batch, indicator,
            Constants.WORLD_WIDTH / 2 - layout.width / 2,
            panelY - 20);
        
        // Botón cerrar
        layout.setText(titleFont, "X");
        titleFont.draw(batch, "X",
            closeViewerButton.x + (closeViewerButton.width - layout.width) / 2,
            closeViewerButton.y + (closeViewerButton.height + layout.height) / 2);
        
        // Flechas
        if (unlockLevel > 1) {
            layout.setText(titleFont, "<");
            titleFont.draw(batch, "<",
                prevVariantButton.x + (prevVariantButton.width - layout.width) / 2,
                prevVariantButton.y + (prevVariantButton.height + layout.height) / 2);
            
            layout.setText(titleFont, ">");
            titleFont.draw(batch, ">",
                nextVariantButton.x + (nextVariantButton.width - layout.width) / 2,
                nextVariantButton.y + (nextVariantButton.height + layout.height) / 2);
        }
        
        // Texto botón desbloqueo
        if (unlockLevel < Constants.ARTS_PER_CHARACTER) {
            int cost = getNextUnlockCost(viewedCharacterId);
            String unlockText = "DESBLOQUEAR: " + cost + " " + Constants.CURRENCY_NAME;
            layout.setText(textFont, unlockText);
            textFont.draw(batch, unlockText,
                unlockButton.x + (unlockButton.width - layout.width) / 2,
                unlockButton.y + (unlockButton.height + layout.height) / 2);
        } else {
            String completeText = "¡COMPLETO!";
            layout.setText(textFont, completeText);
            textFont.setColor(Color.GOLD);
            textFont.draw(batch, completeText,
                Constants.WORLD_WIDTH / 2 - layout.width / 2,
                unlockButton.y + unlockButton.height / 2);
            textFont.setColor(Color.WHITE);
        }
        
        // PCOINS
        String pcoinsText = Constants.CURRENCY_NAME + ": " + getPlayerData().pcoins;
        layout.setText(smallFont, pcoinsText);
        smallFont.draw(batch, pcoinsText,
            Constants.WORLD_WIDTH - layout.width - 20,
            50);
        
        batch.end();
    }
    
    @Override
    public void hide() {
        super.hide();
        if (viewingCharacter) {
            closeCharacterViewer();
        }
    }
    
    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (textFont != null) textFont.dispose();
        if (smallFont != null) smallFont.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}