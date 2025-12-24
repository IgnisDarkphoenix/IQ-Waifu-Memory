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
 * Pantalla de galería.
 * Muestra todos los personajes desbloqueados y permite desbloquear nuevos.
 * Soporta sistema de variantes con marcos por rareza (Base, ★, ★★, ★★★).
 */
public class GalleryScreen extends BaseScreen {

    // ========== CONSTANTES ==========
    private static final int COLUMNS = 4;
    private static final float PADDING = 40f;
    private static final float SPACING = 20f;
    private static final float BUTTON_SIZE = 80f;
    private static final float TOP_BAR_HEIGHT = 130f;
    private static final float DRAG_THRESHOLD = 5f;

    // ========== COLORES ==========
    private static final Color COLOR_TOP_BAR = new Color(0.15f, 0.15f, 0.2f, 1f);
    private static final Color COLOR_BUTTON = new Color(0.3f, 0.3f, 0.4f, 1f);
    private static final Color COLOR_SLOT_LOCKED = new Color(0.15f, 0.15f, 0.18f, 1f);
    private static final Color COLOR_SLOT_BORDER = new Color(0.4f, 0.4f, 0.5f, 1f);
    private static final Color COLOR_VIEWER_BG = new Color(0.1f, 0.1f, 0.15f, 1f);
    private static final Color COLOR_VIEWER_PANEL = new Color(0.2f, 0.2f, 0.25f, 1f);
    private static final Color COLOR_CLOSE_BUTTON = new Color(0.6f, 0.2f, 0.2f, 1f);
    private static final Color COLOR_NAV_BUTTON = new Color(0.3f, 0.3f, 0.5f, 1f);
    private static final Color COLOR_DISABLED = new Color(0.3f, 0.3f, 0.3f, 1f);

    // ========== FUENTES ==========
    private final BitmapFont titleFont;
    private final BitmapFont textFont;
    private final BitmapFont smallFont;
    private final GlyphLayout layout;

    // ========== RENDERIZADO ==========
    private final ShapeRenderer shapeRenderer;

    // ========== GRID DE PERSONAJES ==========
    private Rectangle[] characterSlots;
    private float slotSize;

    // ========== SCROLL ==========
    private float scrollY;
    private float maxScrollY;
    private float lastTouchY;
    private boolean isDragging;

    // ========== BOTONES ==========
    private Rectangle backButton;

    // ========== VISOR DE PERSONAJE ==========
    private boolean viewingCharacter;
    private int viewedCharacterId;
    private int viewedVariant;
    private Rectangle closeViewerButton;
    private Rectangle prevVariantButton;
    private Rectangle nextVariantButton;
    private Rectangle unlockButton;

    // ========== INPUT ==========
    private final Vector3 touchPos;

    // ========== CONSTRUCTOR ==========

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

        // Estado inicial
        scrollY = 0f;
        maxScrollY = 0f;
        lastTouchY = 0f;
        isDragging = false;

        viewingCharacter = false;
        viewedCharacterId = 0;
        viewedVariant = 0;

        createUI();
        setupInput();

        Gdx.app.log(Constants.TAG, "GalleryScreen inicializada");
    }

    // ========== INICIALIZACIÓN UI ==========

    private void createUI() {
        // Botón atrás
        backButton = new Rectangle(PADDING, Constants.WORLD_HEIGHT - 100, BUTTON_SIZE, BUTTON_SIZE);

        // Calcular tamaño de slots
        slotSize = (Constants.WORLD_WIDTH - PADDING * 2 - (COLUMNS - 1) * SPACING) / COLUMNS;

        int totalCharacters = Constants.TOTAL_CHARACTERS;
        int totalRows = (int) Math.ceil((float) totalCharacters / COLUMNS);

        // Crear slots para cada personaje
        characterSlots = new Rectangle[totalCharacters];
        float startY = Constants.WORLD_HEIGHT - 200;

        for (int i = 0; i < totalCharacters; i++) {
            int row = i / COLUMNS;
            int col = i % COLUMNS;

            float x = PADDING + col * (slotSize + SPACING);
            float y = startY - row * (slotSize + SPACING);

            characterSlots[i] = new Rectangle(x, y, slotSize, slotSize);
        }

        // Calcular scroll máximo
        float contentHeight = totalRows * (slotSize + SPACING);
        float visibleHeight = Constants.WORLD_HEIGHT - 250;
        maxScrollY = Math.max(0f, contentHeight - visibleHeight);

        // Botones del visor de personaje
        closeViewerButton = new Rectangle(
            Constants.WORLD_WIDTH - BUTTON_SIZE - 40,
            Constants.WORLD_HEIGHT - BUTTON_SIZE - 40,
            BUTTON_SIZE, BUTTON_SIZE
        );

        prevVariantButton = new Rectangle(
            40,
            Constants.WORLD_HEIGHT / 2f - BUTTON_SIZE / 2f,
            BUTTON_SIZE, BUTTON_SIZE
        );

        nextVariantButton = new Rectangle(
            Constants.WORLD_WIDTH - BUTTON_SIZE - 40,
            Constants.WORLD_HEIGHT / 2f - BUTTON_SIZE / 2f,
            BUTTON_SIZE, BUTTON_SIZE
        );

        // Botón de desbloqueo más ancho para texto largo
        unlockButton = new Rectangle(
            Constants.WORLD_WIDTH / 2f - 200,
            150,
            400, 80
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
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                // No hacer scroll mientras se ve un personaje
                if (viewingCharacter) return false;

                touchPos.set(screenX, screenY, 0);
                viewport.unproject(touchPos);

                float deltaY = lastTouchY - touchPos.y;

                if (Math.abs(deltaY) > DRAG_THRESHOLD) {
                    isDragging = true;
                    scrollY = clamp(scrollY + deltaY, 0f, maxScrollY);
                }

                lastTouchY = touchPos.y;
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
        });
    }

    // ========== MANEJO DE INPUT ==========

    private void handleGalleryClick() {
        // Botón atrás
        if (backButton.contains(touchPos.x, touchPos.y)) {
            audioManager.playButtonClick();
            goToScreen(new HomeScreen(game));
            return;
        }

        // Click en slots de personajes
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
        // Botón cerrar
        if (closeViewerButton.contains(touchPos.x, touchPos.y)) {
            audioManager.playButtonClick();
            closeCharacterViewer();
            return;
        }

        // Botón variante anterior
        if (prevVariantButton.contains(touchPos.x, touchPos.y)) {
            audioManager.playButtonClick();
            changeVariant(-1);
            return;
        }

        // Botón variante siguiente
        if (nextVariantButton.contains(touchPos.x, touchPos.y)) {
            audioManager.playButtonClick();
            changeVariant(1);
            return;
        }

        // Botón desbloquear
        if (unlockButton.contains(touchPos.x, touchPos.y)) {
            tryUnlockNextLevel();
        }
    }

    // ========== VISOR DE PERSONAJE ==========

    private void openCharacterViewer(int characterId) {
        audioManager.playButtonClick();
        viewingCharacter = true;
        viewedCharacterId = characterId;
        viewedVariant = 0;
        Gdx.app.log(Constants.TAG, "Abriendo visor para personaje: " + characterId);
    }

    private void closeCharacterViewer() {
        viewingCharacter = false;

        // Descargar texturas de variantes para liberar memoria
        for (int v = 0; v < Constants.ARTS_PER_CHARACTER; v++) {
            assetManager.unloadCharacterTexture(viewedCharacterId, v);
        }

        Gdx.app.log(Constants.TAG, "Cerrando visor de personaje");
    }

    private void changeVariant(int direction) {
        int unlockLevel = getPlayerData().getCharacterUnlockLevel(viewedCharacterId);
        int maxViewableVariant = Math.max(0, unlockLevel - 1);

        viewedVariant += direction;

        // Wrap around
        if (viewedVariant < 0) {
            viewedVariant = maxViewableVariant;
        } else if (viewedVariant > maxViewableVariant) {
            viewedVariant = 0;
        }
    }

    private void tryUnlockNextLevel() {
        int currentUnlockLevel = getPlayerData().getCharacterUnlockLevel(viewedCharacterId);

        // Ya está completamente desbloqueado
        if (currentUnlockLevel >= Constants.ARTS_PER_CHARACTER) {
            return;
        }

        // Intentar desbloquear
        if (getPlayerData().unlockCharacterLevel(viewedCharacterId)) {
            audioManager.playButtonClick();
            audioManager.playCoinCollect();
            saveProgress();
            Gdx.app.log(Constants.TAG, "Desbloqueado nivel " +
                       (currentUnlockLevel + 1) + " del personaje " + viewedCharacterId);
        }
    }

    // ========== UTILIDADES ==========

    private int getNextUnlockCost(int characterId) {
        int currentLevel = getPlayerData().getCharacterUnlockLevel(characterId);

        switch (currentLevel) {
            case 0: return Constants.GALLERY_BASE_COST;
            case 1: return Constants.GALLERY_STAR1_COST;
            case 2: return Constants.GALLERY_STAR2_COST;
            case 3: return Constants.GALLERY_STAR3_COST;
            default: return -1; // Ya completo
        }
    }

    private String getStatusString(int unlockLevel) {
        switch (unlockLevel) {
            case 0: return "🔒";
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

    private Color getSlotColor(int unlockLevel) {
        if (unlockLevel == 0) {
            return COLOR_SLOT_LOCKED;
        }
        
        // Color más brillante según nivel de desbloqueo
        float r = 0.2f + (unlockLevel * 0.08f);
        float g = 0.15f + (unlockLevel * 0.1f);
        float b = 0.3f + (unlockLevel * 0.08f);
        return new Color(r, g, b, 1f);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private boolean isSlotVisible(float adjustedY) {
        return adjustedY > -slotSize && adjustedY < Constants.WORLD_HEIGHT - TOP_BAR_HEIGHT;
    }

    // ========== UPDATE ==========

    @Override
    protected void update(float delta) {
        // Animaciones futuras si son necesarias
    }

    // ========== DRAW ==========

    @Override
    protected void draw() {
        if (viewingCharacter) {
            drawCharacterViewer();
        } else {
            drawGalleryGrid();
        }
    }

    private void drawGalleryGrid() {
        // ===== FASE 1: Formas =====
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Barra superior
        shapeRenderer.setColor(COLOR_TOP_BAR);
        shapeRenderer.rect(0, Constants.WORLD_HEIGHT - TOP_BAR_HEIGHT, Constants.WORLD_WIDTH, TOP_BAR_HEIGHT);

        // Botón atrás
        shapeRenderer.setColor(COLOR_BUTTON);
        shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);

        // Slots de personajes
        for (int i = 0; i < characterSlots.length; i++) {
            Rectangle slot = characterSlots[i];
            float adjustedY = slot.y + scrollY;

            if (isSlotVisible(adjustedY)) {
                int unlockLevel = getPlayerData().getCharacterUnlockLevel(i);
                shapeRenderer.setColor(getSlotColor(unlockLevel));
                shapeRenderer.rect(slot.x, adjustedY, slot.width, slot.height);
            }
        }

        shapeRenderer.end();

        // ===== FASE 2: Bordes =====
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(COLOR_SLOT_BORDER);

        for (int i = 0; i < characterSlots.length; i++) {
            Rectangle slot = characterSlots[i];
            float adjustedY = slot.y + scrollY;

            if (isSlotVisible(adjustedY)) {
                shapeRenderer.rect(slot.x, adjustedY, slot.width, slot.height);
            }
        }

        shapeRenderer.end();

        // ===== FASE 3: Texto =====
        batch.begin();

        // Título
        String title = "GALERÍA";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title,
            Constants.WORLD_WIDTH / 2f - layout.width / 2f,
            Constants.WORLD_HEIGHT - 45);

        // PCOINS
        String pcoinsText = Constants.CURRENCY_NAME + ": " + getPlayerData().pcoins;
        layout.setText(textFont, pcoinsText);
        textFont.draw(batch, pcoinsText,
            Constants.WORLD_WIDTH - layout.width - 40,
            Constants.WORLD_HEIGHT - 50);

        // Progreso de completado
        float completion = getPlayerData().getGalleryCompletionPercent();
        String progressText = String.format("Completado: %.1f%%", completion);
        layout.setText(smallFont, progressText);
        smallFont.draw(batch, progressText,
            Constants.WORLD_WIDTH / 2f - layout.width / 2f,
            Constants.WORLD_HEIGHT - 100);

        // Texto botón atrás
        layout.setText(textFont, "<");
        textFont.draw(batch, "<",
            backButton.x + (backButton.width - layout.width) / 2f,
            backButton.y + (backButton.height + layout.height) / 2f);

        // Indicadores en slots
        drawSlotIndicators();

        batch.end();
    }

    private void drawSlotIndicators() {
        for (int i = 0; i < characterSlots.length; i++) {
            Rectangle slot = characterSlots[i];
            float adjustedY = slot.y + scrollY;

            if (!isSlotVisible(adjustedY)) continue;

            int unlockLevel = getPlayerData().getCharacterUnlockLevel(i);

            // Número del personaje
            String numText = String.format("#%02d", i + 1);
            layout.setText(smallFont, numText);
            smallFont.setColor(Color.WHITE);
            smallFont.draw(batch, numText, slot.x + 6, adjustedY + slot.height - 6);

            // Estado de desbloqueo
            String statusText = getStatusString(unlockLevel);
            layout.setText(smallFont, statusText);
            
            if (unlockLevel == 0) {
                smallFont.setColor(Color.GRAY);
            } else if (unlockLevel >= Constants.ARTS_PER_CHARACTER) {
                smallFont.setColor(Color.GOLD);
            } else {
                smallFont.setColor(Color.WHITE);
            }
            
            smallFont.draw(batch, statusText,
                slot.x + (slot.width - layout.width) / 2f,
                adjustedY + 28);

            // Costo o estado completo
            if (unlockLevel < Constants.ARTS_PER_CHARACTER) {
                int cost = getNextUnlockCost(i);
                if (cost >= 0) {
                    String costText = cost + " P";
                    layout.setText(smallFont, costText);

                    smallFont.setColor(getPlayerData().pcoins >= cost ? Color.GREEN : Color.RED);
                    smallFont.draw(batch, costText,
                        slot.x + (slot.width - layout.width) / 2f,
                        adjustedY + slot.height / 2f);
                }
            } else {
                smallFont.setColor(Color.GOLD);
                String completeText = "COMPLETO";
                layout.setText(smallFont, completeText);
                smallFont.draw(batch, completeText,
                    slot.x + (slot.width - layout.width) / 2f,
                    adjustedY + slot.height / 2f);
            }

            smallFont.setColor(Color.WHITE);
        }
    }

    private void drawCharacterViewer() {
        int unlockLevel = getPlayerData().getCharacterUnlockLevel(viewedCharacterId);
        boolean canViewVariant = viewedVariant < Math.max(0, unlockLevel);

        // Panel central
        float panelSize = Constants.WORLD_WIDTH - 160;
        float panelX = (Constants.WORLD_WIDTH - panelSize) / 2f;
        float panelY = Constants.WORLD_HEIGHT / 2f - panelSize / 2f + 50;

        // ===== FASE 1: Formas =====
        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Fondo
        shapeRenderer.setColor(COLOR_VIEWER_BG);
        shapeRenderer.rect(0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);

        // Panel central
        shapeRenderer.setColor(COLOR_VIEWER_PANEL);
        shapeRenderer.rect(panelX, panelY, panelSize, panelSize);

        // Botón cerrar
        shapeRenderer.setColor(COLOR_CLOSE_BUTTON);
        shapeRenderer.rect(closeViewerButton.x, closeViewerButton.y,
                          closeViewerButton.width, closeViewerButton.height);

        // Botones de navegación (solo si hay más de una variante)
        if (unlockLevel > 1) {
            shapeRenderer.setColor(COLOR_NAV_BUTTON);
            shapeRenderer.rect(prevVariantButton.x, prevVariantButton.y,
                              prevVariantButton.width, prevVariantButton.height);
            shapeRenderer.rect(nextVariantButton.x, nextVariantButton.y,
                              nextVariantButton.width, nextVariantButton.height);
        }

        // Botón desbloqueo
        if (unlockLevel < Constants.ARTS_PER_CHARACTER) {
            int cost = getNextUnlockCost(viewedCharacterId);
            boolean canAfford = cost >= 0 && getPlayerData().pcoins >= cost;

            if (canAfford) {
                shapeRenderer.setColor(Constants.COLOR_PRIMARY[0],
                                       Constants.COLOR_PRIMARY[1],
                                       Constants.COLOR_PRIMARY[2], 1f);
            } else {
                shapeRenderer.setColor(COLOR_DISABLED);
            }

            shapeRenderer.rect(unlockButton.x, unlockButton.y,
                              unlockButton.width, unlockButton.height);
        }

        shapeRenderer.end();

        // ===== FASE 2: Imagen y texto =====
        batch.begin();

        // Dibujar imagen del personaje y marco
        if (canViewVariant) {
            drawCharacterImage(panelX, panelY, panelSize);
        } else {
            drawLockedPlaceholder();
        }

        // Textos del visor
        drawViewerTexts(panelY, unlockLevel);

        // Botones de navegación
        drawViewerButtons(unlockLevel);

        batch.end();
    }

    private void drawCharacterImage(float panelX, float panelY, float panelSize) {
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
                Constants.WORLD_WIDTH / 2f - layout.width / 2f,
                Constants.WORLD_HEIGHT / 2f + layout.height / 2f);
        }
    }

    private void drawLockedPlaceholder() {
        titleFont.setColor(Color.GRAY);
        String lockedText = "BLOQUEADO";
        layout.setText(titleFont, lockedText);
        titleFont.draw(batch, lockedText,
            Constants.WORLD_WIDTH / 2f - layout.width / 2f,
            Constants.WORLD_HEIGHT / 2f);
        titleFont.setColor(Color.WHITE);
    }

    private void drawViewerTexts(float panelY, int unlockLevel) {
        // Nombre del personaje
        String charName = "Personaje #" + (viewedCharacterId + 1);
        layout.setText(titleFont, charName);
        titleFont.draw(batch, charName,
            Constants.WORLD_WIDTH / 2f - layout.width / 2f,
            Constants.WORLD_HEIGHT - 60);

        // Variante actual
        String variantText = "Variante: " + getVariantName(viewedVariant);
        layout.setText(textFont, variantText);
        textFont.draw(batch, variantText,
            Constants.WORLD_WIDTH / 2f - layout.width / 2f,
            Constants.WORLD_HEIGHT - 110);

        // Indicador de variante
        int maxViewable = Math.max(1, unlockLevel);
        String indicator = (viewedVariant + 1) + "/" + maxViewable;
        layout.setText(textFont, indicator);
        textFont.draw(batch, indicator,
            Constants.WORLD_WIDTH / 2f - layout.width / 2f,
            panelY - 20);

        // Texto botón desbloqueo o estado completo
        if (unlockLevel < Constants.ARTS_PER_CHARACTER) {
            int cost = getNextUnlockCost(viewedCharacterId);
            String unlockText = (cost >= 0)
                ? "DESBLOQUEAR: " + cost + " " + Constants.CURRENCY_NAME
                : "DESBLOQUEAR";

            layout.setText(textFont, unlockText);
            textFont.draw(batch, unlockText,
                unlockButton.x + (unlockButton.width - layout.width) / 2f,
                unlockButton.y + (unlockButton.height + layout.height) / 2f);
        } else {
            textFont.setColor(Color.GOLD);
            String completeText = "¡COMPLETO!";
            layout.setText(textFont, completeText);
            textFont.draw(batch, completeText,
                Constants.WORLD_WIDTH / 2f - layout.width / 2f,
                unlockButton.y + unlockButton.height / 2f);
            textFont.setColor(Color.WHITE);
        }

        // PCOINS
        String pcoinsText = Constants.CURRENCY_NAME + ": " + getPlayerData().pcoins;
        layout.setText(smallFont, pcoinsText);
        smallFont.draw(batch, pcoinsText,
            Constants.WORLD_WIDTH - layout.width - 20,
            50);
    }

    private void drawViewerButtons(int unlockLevel) {
        // Botón cerrar (X)
        layout.setText(titleFont, "X");
        titleFont.draw(batch, "X",
            closeViewerButton.x + (closeViewerButton.width - layout.width) / 2f,
            closeViewerButton.y + (closeViewerButton.height + layout.height) / 2f);

        // Flechas de navegación
        if (unlockLevel > 1) {
            layout.setText(titleFont, "<");
            titleFont.draw(batch, "<",
                prevVariantButton.x + (prevVariantButton.width - layout.width) / 2f,
                prevVariantButton.y + (prevVariantButton.height + layout.height) / 2f);

            layout.setText(titleFont, ">");
            titleFont.draw(batch, ">",
                nextVariantButton.x + (nextVariantButton.width - layout.width) / 2f,
                nextVariantButton.y + (nextVariantButton.height + layout.height) / 2f);
        }
    }

    // ========== LIFECYCLE ==========

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
        Gdx.app.log(Constants.TAG, "GalleryScreen disposed");
    }
}