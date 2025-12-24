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
 * Maneja el grid de cartas del juego
 */
public class GameGrid {
    
    private int gridSize;
    private Card[] cards;
    private int totalCards;
    
    // Referencia al GameScreen para acceder al AssetManager
    private GameScreen gameScreen;
    
    // Posición del grid
    private float gridX, gridY;
    private float cardSize;
    private float spacing;
    
    // Textura del reverso
    private Texture cardBackTexture;
    
    // Marcos por rareza
    private Texture frameBase;
    
    public GameGrid(int size, GameScreen gameScreen) {
        this.gridSize = size;
        this.gameScreen = gameScreen;
        this.totalCards = size * size;
        this.cards = new Card[totalCards];
        
        // Calcular tamaño de cartas para que quepan en pantalla
        calculateLayout();
        
        // Obtener texturas
        cardBackTexture = gameScreen.getAssetManager().getCardBackTexture();
        frameBase = gameScreen.getAssetManager().getFrameTexture(0); // Marco base para gameplay
        
        // Crear cartas
        createCards();
    }
    
    private void calculateLayout() {
        // Área disponible para el grid
        float availableWidth = Constants.WORLD_WIDTH - 80; // Padding lateral
        float availableHeight = Constants.WORLD_HEIGHT - 350; // Espacio para HUD arriba y abajo
        
        // Calcular tamaño de carta
        float maxCardWidth = (availableWidth - (gridSize - 1) * Constants.CARD_PADDING) / gridSize;
        float maxCardHeight = (availableHeight - (gridSize - 1) * Constants.CARD_PADDING) / gridSize;
        
        cardSize = Math.min(maxCardWidth, maxCardHeight);
        spacing = Constants.CARD_PADDING;
        
        // Calcular posición centrada del grid
        float gridWidth = gridSize * cardSize + (gridSize - 1) * spacing;
        float gridHeight = gridSize * cardSize + (gridSize - 1) * spacing;
        
        gridX = (Constants.WORLD_WIDTH - gridWidth) / 2;
        gridY = (Constants.WORLD_HEIGHT - gridHeight) / 2 - 30; // Un poco hacia abajo por el HUD
    }
    
    private void createCards() {
        int numPairs = totalCards / 2;
        
        // Crear array de IDs de personajes (cada ID aparece 2 veces)
        int[] characterIds = new int[totalCards];
        for (int i = 0; i < numPairs; i++) {
            // Usar IDs del 0 al 49 (50 personajes disponibles), con wrap around
            int charId = i % Constants.TOTAL_CHARACTERS;
            characterIds[i * 2] = charId;
            characterIds[i * 2 + 1] = charId;
        }
        
        // Mezclar el array
        shuffleArray(characterIds);
        
        // Crear las cartas
        for (int i = 0; i < totalCards; i++) {
            int charId = characterIds[i];
            
            // Obtener textura del personaje (lazy loading)
            // En gameplay usamos variante 0 (base)
            Texture frontTexture = gameScreen.getAssetManager().getCharacterTexture(charId, 0);
            
            // Crear carta con marco base
            Card card = new Card(charId, 0, frontTexture, cardBackTexture, frameBase);
            card.setGridIndex(i);
            
            // Calcular posición
            int row = i / gridSize;
            int col = i % gridSize;
            float x = gridX + col * (cardSize + spacing);
            float y = gridY + (gridSize - 1 - row) * (cardSize + spacing); // Invertir Y para que fila 0 esté arriba
            
            card.setPosition(x, y);
            card.setSize(cardSize, cardSize);
            
            cards[i] = card;
        }
    }
    
    /**
     * Mezcla un array de enteros (Fisher-Yates shuffle)
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
     * Mezcla las cartas no emparejadas
     */
    public void shuffleUnmatched() {
        // Recolectar índices de cartas no emparejadas
        Array<Integer> unmatchedIndices = new Array<>();
        for (int i = 0; i < totalCards; i++) {
            if (!cards[i].isMatched()) {
                unmatchedIndices.add(i);
            }
        }
        
        if (unmatchedIndices.size <= 1) return;
        
        // Guardar posiciones originales
        float[] originalX = new float[unmatchedIndices.size];
        float[] originalY = new float[unmatchedIndices.size];
        
        for (int i = 0; i < unmatchedIndices.size; i++) {
            int idx = unmatchedIndices.get(i);
            originalX[i] = cards[idx].getX();
            originalY[i] = cards[idx].getY();
        }
        
        // Mezclar posiciones
        for (int i = unmatchedIndices.size - 1; i > 0; i--) {
            int j = MathUtils.random(i);
            
            // Intercambiar posiciones
            float tempX = originalX[i];
            float tempY = originalY[i];
            originalX[i] = originalX[j];
            originalY[i] = originalY[j];
            originalX[j] = tempX;
            originalY[j] = tempY;
        }
        
        // Aplicar nuevas posiciones
        for (int i = 0; i < unmatchedIndices.size; i++) {
            int idx = unmatchedIndices.get(i);
            cards[idx].setPosition(originalX[i], originalY[i]);
            
            // Ocultar cartas reveladas durante el shuffle
            if (cards[idx].isRevealed() && !cards[idx].isMatched()) {
                cards[idx].hideInstant();
            }
        }
    }
    
    /**
     * Actualiza todas las cartas
     */
    public void update(float delta) {
        for (Card card : cards) {
            if (card != null) {
                card.update(delta);
            }
        }
    }
    
    /**
     * Dibuja el grid y las cartas
     */
    public void draw(SpriteBatch batch, ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        // ===== FASE 1: Dibujar fondos con ShapeRenderer =====
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Fondo semi-transparente del área de juego
        float gridWidth = gridSize * cardSize + (gridSize - 1) * spacing;
        float gridHeight = gridSize * cardSize + (gridSize - 1) * spacing;
        
        shapeRenderer.setColor(0.08f, 0.08f, 0.12f, 0.7f);
        shapeRenderer.rect(gridX - 15, gridY - 15, gridWidth + 30, gridHeight + 30);
        
        // Dibujar placeholders para cartas sin textura
        boolean hasAnyMissingTexture = false;
        for (Card card : cards) {
            if (card != null) {
                boolean showingFront = card.isRevealed() || (card.isAnimating() && card.isRevealed());
                
                // Si no tiene textura frontal y está mostrando el frente
                if (showingFront && !card.hasFrontTexture()) {
                    hasAnyMissingTexture = true;
                    card.drawPlaceholder(shapeRenderer, true);
                }
                // Si no tiene textura trasera y está mostrando el reverso
                else if (!showingFront && !card.hasBackTexture()) {
                    hasAnyMissingTexture = true;
                    card.drawPlaceholder(shapeRenderer, false);
                }
            }
        }
        
        shapeRenderer.end();
        
        // ===== FASE 2: Dibujar cartas con SpriteBatch =====
        batch.begin();
        
        for (Card card : cards) {
            if (card != null) {
                // Solo dibujar con batch si tiene las texturas necesarias
                boolean showingFront = card.isRevealed() || 
                                       (card.isAnimating() && !card.isRevealed() == false);
                
                if (showingFront && card.hasFrontTexture()) {
                    card.draw(batch);
                } else if (!showingFront && card.hasBackTexture()) {
                    card.draw(batch);
                }
                // Si le faltan texturas, ya se dibujó el placeholder arriba
            }
        }
        
        batch.end();
        
        // ===== FASE 3: Dibujar bordes (debug/visual) =====
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.4f, 0.5f);
        
        for (Card card : cards) {
            if (card != null && !card.isMatched()) {
                shapeRenderer.rect(card.getX(), card.getY(), card.getWidth(), card.getHeight());
            }
        }
        
        shapeRenderer.end();
    }
    
    /**
     * Obtiene la carta en una posición de pantalla
     */
    public Card getCardAt(float x, float y) {
        for (Card card : cards) {
            if (card != null && card.contains(x, y)) {
                return card;
            }
        }
        return null;
    }
    
    /**
     * Obtiene todas las cartas
     */
    public Card[] getCards() {
        return cards;
    }
    
    /**
     * Obtiene el número de cartas emparejadas
     */
    public int getMatchedCount() {
        int count = 0;
        for (Card card : cards) {
            if (card != null && card.isMatched()) {
                count++;
            }
        }
        return count / 2; // Retorna número de pares
    }
    
    /**
     * Verifica si todas las cartas están emparejadas
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
     * Libera recursos
     */
    public void dispose() {
        // Las texturas son manejadas por AssetManager, no las liberamos aquí
    }
}