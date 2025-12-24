package com.waifu.memory.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.waifu.memory.utils.Constants;

/**
 * Representa una carta individual en el juego
 */
public class Card {
    
    // Posición y tamaño
    private float x, y;
    private float width, height;
    private Rectangle bounds;
    
    // Identificador del personaje (para matching)
    private int characterId;
    
    // Texturas
    private Texture frontTexture;  // Imagen del personaje
    private Texture backTexture;   // Reverso de la carta
    
    // Estados
    private boolean revealed;      // Carta volteada mostrando frente
    private boolean matched;       // Par encontrado
    private boolean animating;     // En animación de flip
    
    // Animación de flip
    private float flipProgress;    // 0 = back, 1 = front
    private float flipDirection;   // 1 = voltear a frente, -1 = voltear a reverso
    private float flipSpeed;
    
    // Índice en el grid
    private int gridIndex;
    
    public Card(int characterId, Texture frontTexture, Texture backTexture) {
        this.characterId = characterId;
        this.frontTexture = frontTexture;
        this.backTexture = backTexture;
        
        this.width = Constants.CARD_SIZE;
        this.height = Constants.CARD_SIZE;
        this.bounds = new Rectangle();
        
        this.revealed = false;
        this.matched = false;
        this.animating = false;
        this.flipProgress = 0f;
        this.flipDirection = 0f;
        this.flipSpeed = 1f / Constants.CARD_FLIP_TIME;
    }
    
    /**
     * Establece la posición de la carta
     */
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        bounds.set(x, y, width, height);
    }
    
    /**
     * Establece el tamaño de la carta
     */
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        bounds.set(x, y, width, height);
    }
    
    /**
     * Actualiza la animación de la carta
     */
    public void update(float delta) {
        if (animating) {
            flipProgress += flipDirection * flipSpeed * delta;
            
            if (flipProgress >= 1f) {
                flipProgress = 1f;
                animating = false;
                revealed = true;
            } else if (flipProgress <= 0f) {
                flipProgress = 0f;
                animating = false;
                revealed = false;
            }
        }
    }
    
    /**
     * Dibuja la carta
     */
    public void draw(SpriteBatch batch) {
        if (matched && revealed) {
            // Carta emparejada - efecto especial
            drawWithAlpha(batch, frontTexture, 0.7f);
            return;
        }
        
        // Calcular escala para efecto 3D de flip
        float scaleX = Math.abs(flipProgress - 0.5f) * 2f;
        if (scaleX < 0.1f) scaleX = 0.1f;
        
        // Determinar qué textura mostrar
        Texture textureToDraw;
        if (flipProgress < 0.5f) {
            textureToDraw = backTexture;
        } else {
            textureToDraw = frontTexture;
        }
        
        // Dibujar con efecto de escala
        float drawWidth = width * scaleX;
        float offsetX = (width - drawWidth) / 2;
        
        if (textureToDraw != null) {
            batch.draw(textureToDraw, x + offsetX, y, drawWidth, height);
        }
    }
    
    private void drawWithAlpha(SpriteBatch batch, Texture texture, float alpha) {
        if (texture == null) return;
        
        batch.setColor(1, 1, 1, alpha);
        batch.draw(texture, x, y, width, height);
        batch.setColor(1, 1, 1, 1);
    }
    
    /**
     * Voltea la carta para mostrar el frente
     */
    public void flip() {
        if (!revealed && !animating && !matched) {
            animating = true;
            flipDirection = 1f;
        }
    }
    
    /**
     * Voltea la carta de regreso al reverso
     */
    public void flipBack() {
        if (revealed && !animating && !matched) {
            animating = true;
            flipDirection = -1f;
        }
    }
    
    /**
     * Revela instantáneamente (para preview inicial)
     */
    public void revealInstant() {
        revealed = true;
        flipProgress = 1f;
        animating = false;
    }
    
    /**
     * Oculta instantáneamente
     */
    public void hideInstant() {
        revealed = false;
        flipProgress = 0f;
        animating = false;
    }
    
    /**
     * Verifica si un punto está dentro de la carta
     */
    public boolean contains(float px, float py) {
        return bounds.contains(px, py);
    }
    
    // Getters y Setters
    
    public int getCharacterId() {
        return characterId;
    }
    
    public boolean isRevealed() {
        return revealed;
    }
    
    public boolean isMatched() {
        return matched;
    }
    
    public void setMatched(boolean matched) {
        this.matched = matched;
    }
    
    public boolean isAnimating() {
        return animating;
    }
    
    public int getGridIndex() {
        return gridIndex;
    }
    
    public void setGridIndex(int index) {
        this.gridIndex = index;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public float getWidth() {
        return width;
    }
    
    public float getHeight() {
        return height;
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
}