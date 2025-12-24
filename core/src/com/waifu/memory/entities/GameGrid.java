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
    
    public GameGrid(int size, GameScreen gameScreen) {
        this.gridSize = size;
        this.gameScreen = gameScreen;
        this.totalCards = size * size;
        this.cards = new Card[totalCards];
        
        // Calcular tamaño de cartas para que quepan en pantalla
        calculateLayout();
        
        // Obtener textura del reverso
        cardBackTexture = gameScreen.getAssetManager().getCardBackTexture();
        
        // Crear cartas
        createCards();
    }
    
    private void calculateLayout() {
        // Área disponible para el grid
        float availableWidth = Constants.WORLD_WIDTH - 80; // Padding lateral
        float availableHeight = Constants.WORLD_HEIGHT - 300; // Espacio para HUD
        
        // Calcular tamaño de carta
        float maxCardWidth = (availableWidth - (gridSize - 1) * Constants.CARD_PADDING) / gridSize;
        float maxCardHeight = (availableHeight - (gridSize - 1) * Constants.CARD_PADDING) / gridSize;
        
        cardSize = Math.min(maxCardWidth, maxCardHeight);
        spacing = Constants.CARD_PADDING;
        
        // Calcular posición centrada del grid
        float gridWidth = gridSize * cardSize + (gridSize - 1) * spacing;
        float gridHeight = gridSize * cardSize + (gridSize - 1) * spacing;
        
        gridX = (Constants.WORLD_WIDTH - gridWidth) / 2;
        gridY = (Constants.WORLD_HEIGHT - gridHeight) / 2 - 50; // Un poco hacia abajo por el HUD
    }
    
    private void createCards() {
        int numPairs = totalCards / 2;
        
        // Crear array de IDs de personajes (cada ID aparece 2 veces)
        int[] characterIds = new int[totalCards];
        for (int i = 0; i < numPairs; i++) {
            // Usar IDs del 0 al 49 (50 personajes disponibles)
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
            Texture frontTexture = gameScreen.getAssetManager().getCharacterTexture(charId, 0);
            
            // Crear carta
            Card card = new Card(charId, frontTexture, cardBackTexture);
            card.setGridIndex(i);
            
            // Calcular posición
            int row = i / gridSize;
            int col = i % gridSize;
            float x = gridX + col * (cardSize + spacing);
            float y = gridY + (gridSize - 1 - row) * (cardSize + spacing); // Invertir Y
            
            card.setPosition(x, y);
            card.setSize(cardSize, cardSize);
            
            cards[i] = card;
        }
    }
    
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
            card.update(delta);
        }
    }
    
    /**
     * Dibuja el grid y las cartas
     */
    public void draw(SpriteBatch batch, ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        // Dibujar fondo del grid (opcional)
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Fondo semi-transparente del área de juego
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.5f);
        float gridWidth = gridSize * cardSize + (gridSize - 1) * spacing;
        float gridHeight = gridSize * cardSize + (gridSize - 1) * spacing;
        shapeRenderer.rect(gridX - 20, gridY - 20, gridWidth + 40, gridHeight + 40);
        
        shapeRenderer.end();
        
        // Dibujar cartas
        batch.begin();
        
        for (Card card : cards) {
            // Dibujar placeholder si no hay textura
            if (card != null) {
                card.draw(batch);
            }
        }
        
        batch.end();
        
        // Dibujar bordes de cartas (debug/placeholder)
        if (cardBackTexture == null) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.WHITE);
            
            for (Card card : cards) {
                if (card != null && !card.isMatched()) {
                    shapeRenderer.rect(card.getX(), card.getY(), card.getWidth(), card.getHeight());
                }
            }
            
            shapeRenderer.end();
        }
    }
    
    /**
     * Obtiene la carta en una posición de pantalla
     */
    public Card getCardAt(float x, float y) {
        for (Card card : cards) {
            if (card.contains(x, y)) {
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
     * Libera recursos
     */
    public void dispose() {
        // Las texturas son manejadas por AssetManager
    }
}