package com.waifu.memory.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.waifu.memory.screens.GameScreen;
import com.waifu.memory.utils.Constants;

/**
 * Maneja el grid de cartas del juego.
 * Soporta diferentes tamaños de grid y mecánica de shuffle.
 */
public class GameGrid {

    // ========== CONFIGURACIÓN DEL GRID ==========
    private final int gridSize;
    private final int totalCards;
    private final Card[] cards;

    // ========== REFERENCIAS ==========
    private final GameScreen gameScreen;

    // ========== LAYOUT ==========
    private float gridX, gridY;
    private float cardSize;
    private float spacing;

    // ========== TEXTURAS COMPARTIDAS ==========
    private final Texture cardBackTexture;
    private final Texture baseFrameTexture;

    // ========== CONSTANTES DE LAYOUT ==========
    private static final float HORIZONTAL_PADDING = 80f;
    private static final float VERTICAL_PADDING = 320f;
    private static final float GRID_Y_OFFSET = -40f;
    private static final float BACKGROUND_PADDING = 20f;

    // ========== COLORES ==========
    private static final Color BACKGROUND_COLOR = new Color(0.08f, 0.08f, 0.12f, 0.7f);
    private static final Color DEBUG_BORDER_COLOR = new Color(0.3f, 0.3f, 0.4f, 0.5f);

    // ========== CONSTRUCTOR ==========

    /**
     * Crea un nuevo grid de cartas
     * @param size Tamaño del grid (4, 6, u 8 para 4x4, 6x6, 8x8)
     * @param gameScreen Referencia al GameScreen para acceder a recursos
     */
    public GameGrid(int size, GameScreen gameScreen) {
        this.gridSize = size;
        this.gameScreen = gameScreen;
        this.totalCards = size * size;
        this.cards = new Card[totalCards];

        // Calcular layout del grid
        calculateLayout();

        // Obtener texturas compartidas del AssetManager
        this.cardBackTexture = gameScreen.getAssetManager().getCardBackTexture();
        this.baseFrameTexture = gameScreen.getAssetManager().getFrameTexture(0);

        // Crear las cartas
        createCards();
    }

    // ========== CONFIGURACIÓN DE LAYOUT ==========

    /**
     * Calcula el tamaño y posición del grid basado en el espacio disponible
     */
    private void calculateLayout() {
        // Área disponible para el grid
        float availableWidth = Constants.WORLD_WIDTH - HORIZONTAL_PADDING;
        float availableHeight = Constants.WORLD_HEIGHT - VERTICAL_PADDING;

        // Calcular tamaño máximo de carta que quepa
        float maxCardWidth = (availableWidth - (gridSize - 1) * Constants.CARD_PADDING) / gridSize;
        float maxCardHeight = (availableHeight - (gridSize - 1) * Constants.CARD_PADDING) / gridSize;

        cardSize = Math.min(maxCardWidth, maxCardHeight);
        spacing = Constants.CARD_PADDING;

        // Calcular dimensiones totales del grid
        float gridWidth = gridSize * cardSize + (gridSize - 1) * spacing;
        float gridHeight = gridSize * cardSize + (gridSize - 1) * spacing;

        // Centrar el grid en pantalla
        gridX = (Constants.WORLD_WIDTH - gridWidth) / 2f;
        gridY = (Constants.WORLD_HEIGHT - gridHeight) / 2f + GRID_Y_OFFSET;
    }

    /**
     * Crea todas las cartas del grid
     */
    private void createCards() {
        int numPairs = totalCards / 2;

        // Crear array de IDs de personajes (cada ID aparece 2 veces)
        int[] characterIds = new int[totalCards];
        for (int i = 0; i < numPairs; i++) {
            // Usar IDs del 0 al TOTAL_CHARACTERS-1, con wrap around
            int charId = i % Constants.TOTAL_CHARACTERS;
            characterIds[i * 2] = charId;
            characterIds[i * 2 + 1] = charId;
        }

        // Mezclar el array
        shuffleArray(characterIds);

        // Crear las cartas
        for (int i = 0; i < totalCards; i++) {
            int charId = characterIds[i];

            // Obtener textura del personaje (variante base para gameplay)
            Texture frontTexture = gameScreen.getAssetManager().getCharacterTexture(charId, 0);

            // Crear carta con marco base
            Card card = new Card(charId, 0, frontTexture, cardBackTexture, baseFrameTexture);
            card.setGridIndex(i);

            // Calcular posición en el grid
            int row = i / gridSize;
            int col = i % gridSize;

            float x = gridX + col * (cardSize + spacing);
            // Invertir Y para que fila 0 esté arriba
            float y = gridY + (gridSize - 1 - row) * (cardSize + spacing);

            card.setPosition(x, y);
            card.setSize(cardSize, cardSize);

            cards[i] = card;
        }
    }

    // ========== UTILIDADES DE MEZCLA ==========

    /**
     * Mezcla un array de enteros usando Fisher-Yates shuffle
     */
    private void shuffleArray(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = MathUtils.random(i);
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    /**
     * Mezcla las posiciones de las cartas no emparejadas.
     * Útil para aumentar la dificultad durante el juego.
     */
    public void shuffleUnmatched() {
        // Recolectar índices de cartas no emparejadas
        Array<Integer> unmatchedIndices = new Array<>();
        for (int i = 0; i < totalCards; i++) {
            if (cards[i] != null && !cards[i].isMatched()) {
                unmatchedIndices.add(i);
            }
        }

        // Si hay 0 o 1 carta, no hay nada que mezclar
        if (unmatchedIndices.size <= 1) return;

        // Guardar posiciones originales
        float[] originalX = new float[unmatchedIndices.size];
        float[] originalY = new float[unmatchedIndices.size];

        for (int i = 0; i < unmatchedIndices.size; i++) {
            int idx = unmatchedIndices.get(i);
            originalX[i] = cards[idx].getX();
            originalY[i] = cards[idx].getY();
        }

        // Mezclar posiciones (Fisher-Yates)
        for (int i = unmatchedIndices.size - 1; i > 0; i--) {
            int j = MathUtils.random(i);

            // Intercambiar posiciones X
            float tempX = originalX[i];
            originalX[i] = originalX[j];
            originalX[j] = tempX;

            // Intercambiar posiciones Y
            float tempY = originalY[i];
            originalY[i] = originalY[j];
            originalY[j] = tempY;
        }

        // Aplicar nuevas posiciones y ocultar cartas reveladas
        for (int i = 0; i < unmatchedIndices.size; i++) {
            int idx = unmatchedIndices.get(i);
            cards[idx].setPosition(originalX[i], originalY[i]);

            // Ocultar cartas reveladas durante el shuffle
            if (cards[idx].isRevealed() && !cards[idx].isMatched()) {
                cards[idx].hideInstant();
            }
        }
    }

    // ========== ACTUALIZACIÓN ==========

    /**
     * Actualiza todas las cartas del grid
     * @param delta Tiempo desde el último frame
     */
    public void update(float delta) {
        for (Card card : cards) {
            if (card != null) {
                card.update(delta);
            }
        }
    }

    // ========== RENDERIZADO ==========

    /**
     * Dibuja el grid y todas las cartas
     * @param batch SpriteBatch para dibujar texturas
     * @param shapeRenderer ShapeRenderer para fondos y debug
     * @param camera Cámara para proyección
     */
    public void draw(SpriteBatch batch, ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        // Calcular dimensiones del grid
        float gridWidth = gridSize * cardSize + (gridSize - 1) * spacing;
        float gridHeight = gridSize * cardSize + (gridSize - 1) * spacing;

        // ===== FASE 1: Fondo y Placeholders (ShapeRenderer) =====
        drawBackgroundAndPlaceholders(shapeRenderer, camera, gridWidth, gridHeight);

        // ===== FASE 2: Cartas (SpriteBatch) =====
        drawCards(batch);

        // ===== FASE 3: Bordes Debug (ShapeRenderer) =====
        drawDebugBorders(shapeRenderer, camera);
    }

    /**
     * Dibuja el fondo del área de juego y placeholders para cartas sin textura
     */
    private void drawBackgroundAndPlaceholders(ShapeRenderer shapeRenderer, OrthographicCamera camera,
                                                float gridWidth, float gridHeight) {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Fondo semi-transparente del área de juego
        shapeRenderer.setColor(BACKGROUND_COLOR);
        shapeRenderer.rect(
            gridX - BACKGROUND_PADDING,
            gridY - BACKGROUND_PADDING,
            gridWidth + BACKGROUND_PADDING * 2,
            gridHeight + BACKGROUND_PADDING * 2
        );

        // Dibujar placeholders para cartas sin textura
        for (Card card : cards) {
            if (card == null) continue;

            boolean showingFront = card.isRevealed() || 
                                   (card.isAnimating() && card.isRevealed());

            // Si no tiene la textura necesaria, dibujar placeholder
            if (showingFront && !card.hasFrontTexture()) {
                card.drawPlaceholder(shapeRenderer, true);
            } else if (!showingFront && !card.hasBackTexture()) {
                card.drawPlaceholder(shapeRenderer, false);
            }
        }

        shapeRenderer.end();
    }

    /**
     * Dibuja las cartas que tienen texturas
     */
    private void drawCards(SpriteBatch batch) {
        batch.begin();

        for (Card card : cards) {
            if (card == null) continue;

            boolean showingFront = card.isRevealed() || 
                                   (card.isAnimating() && card.isRevealed());

            // Solo dibujar si tiene la textura necesaria
            boolean canDraw = (showingFront && card.hasFrontTexture()) ||
                             (!showingFront && card.hasBackTexture());

            if (canDraw) {
                card.draw(batch);
            }
        }

        batch.end();
    }

    /**
     * Dibuja bordes de debug para cartas sin textura o para visualización
     */
    private void drawDebugBorders(ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        // Solo dibujar si faltan texturas (modo desarrollo)
        boolean missingTextures = cardBackTexture == null || hasMissingTextures();

        if (!missingTextures) return;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(DEBUG_BORDER_COLOR);

        for (Card card : cards) {
            if (card != null && !card.isMatched()) {
                shapeRenderer.rect(card.getX(), card.getY(), card.getWidth(), card.getHeight());
            }
        }

        shapeRenderer.end();
    }

    /**
     * Verifica si hay cartas con texturas faltantes
     */
    private boolean hasMissingTextures() {
        for (Card card : cards) {
            if (card != null && (!card.hasFrontTexture() || !card.hasBackTexture())) {
                return true;
            }
        }
        return false;
    }

    // ========== INTERACCIÓN ==========

    /**
     * Obtiene la carta en una posición de pantalla
     * @param x Coordenada X
     * @param y Coordenada Y
     * @return La carta en esa posición o null
     */
    public Card getCardAt(float x, float y) {
        for (Card card : cards) {
            if (card != null && card.contains(x, y)) {
                return card;
            }
        }
        return null;
    }

    // ========== ESTADO DEL JUEGO ==========

    /**
     * Obtiene el número de pares encontrados
     * @return Cantidad de pares matched
     */
    public int getMatchedPairCount() {
        int count = 0;
        for (Card card : cards) {
            if (card != null && card.isMatched()) {
                count++;
            }
        }
        return count / 2; // Cada par son 2 cartas
    }

    /**
     * Obtiene el número total de pares en el grid
     * @return Cantidad total de pares
     */
    public int getTotalPairCount() {
        return totalCards / 2;
    }

    /**
     * Verifica si todas las cartas están emparejadas (juego completado)
     * @return true si el juego está completo
     */
    public boolean isAllMatched() {
        for (Card card : cards) {
            if (card != null && !card.isMatched()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Obtiene el porcentaje de progreso del juego
     * @return Valor entre 0.0 y 1.0
     */
    public float getProgress() {
        int totalPairs = getTotalPairCount();
        if (totalPairs == 0) return 0f;
        return (float) getMatchedPairCount() / totalPairs;
    }

    // ========== GETTERS ==========

    /**
     * Obtiene todas las cartas del grid
     */
    public Card[] getCards() {
        return cards;
    }

    /**
     * Obtiene el tamaño del grid
     */
    public int getGridSize() {
        return gridSize;
    }

    /**
     * Obtiene el número total de cartas
     */
    public int getTotalCards() {
        return totalCards;
    }

    /**
     * Obtiene la posición X del grid
     */
    public float getGridX() {
        return gridX;
    }

    /**
     * Obtiene la posición Y del grid
     */
    public float getGridY() {
        return gridY;
    }

    /**
     * Obtiene el tamaño de cada carta
     */
    public float getCardSize() {
        return cardSize;
    }

    // ========== LIMPIEZA ==========

    /**
     * Resetea todas las cartas a su estado inicial
     */
    public void reset() {
        for (Card card : cards) {
            if (card != null) {
                card.reset();
            }
        }
    }

    /**
     * Libera recursos.
     * Nota: Las texturas son manejadas por AssetManager
     */
    public void dispose() {
        // Las texturas son manejadas por AssetManager, no las liberamos aquí
    }
}