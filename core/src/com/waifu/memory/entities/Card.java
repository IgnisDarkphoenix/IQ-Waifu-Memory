package com.waifu.memory.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.waifu.memory.utils.Constants;

/**
 * Representa una carta individual en el juego
 * Soporta sistema de marcos por rareza
 */
public class Card {
    
    // Posición y tamaño
    private float x, y;
    private float width, height;
    private Rectangle bounds;
    
    // Identificador del personaje (para matching)
    private int characterId;
    
    // Rareza/Variante (0=base, 1=☆, 2=☆☆, 3=☆☆☆)
    private int rarity;
    
    // Texturas
    private Texture frontTexture;  // Imagen del personaje
    private Texture backTexture;   // Reverso de la carta
    private Texture frameTexture;  // Marco según rareza
    
    // Estados
    private boolean revealed;      // Carta volteada mostrando frente
    private boolean matched;       // Par encontrado
    private boolean animating;     // En animación de flip
    
    // Animación de flip
    private float flipProgress;    // 0 = back, 1 = front
    private float flipDirection;   // 1 = voltear a frente, -1 = voltear a reverso
    private float flipSpeed;
    
    // Animación de match (brillo/pulso)
    private float matchGlowAlpha;
    private float matchGlowDirection;
    
    // Animación de escala al hacer match
    private float matchScaleEffect;
    private float matchScaleTimer;
    
    // Índice en el grid
    private int gridIndex;
    
    /**
     * Constructor básico (para gameplay, usa variante 0)
     */
    public Card(int characterId, Texture frontTexture, Texture backTexture) {
        this(characterId, 0, frontTexture, backTexture, null);
    }
    
    /**
     * Constructor completo con rareza y marco
     */
    public Card(int characterId, int rarity, Texture frontTexture, Texture backTexture, Texture frameTexture) {
        this.characterId = characterId;
        this.rarity = rarity;
        this.frontTexture = frontTexture;
        this.backTexture = backTexture;
        this.frameTexture = frameTexture;
        
        this.width = Constants.CARD_SIZE;
        this.height = Constants.CARD_SIZE;
        this.bounds = new Rectangle();
        
        this.revealed = false;
        this.matched = false;
        this.animating = false;
        this.flipProgress = 0f;
        this.flipDirection = 0f;
        this.flipSpeed = 1f / Constants.CARD_FLIP_TIME;
        
        this.matchGlowAlpha = 0f;
        this.matchGlowDirection = 1f;
        this.matchScaleEffect = 1f;
        this.matchScaleTimer = 0f;
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
     * Establece el marco de la carta
     */
    public void setFrameTexture(Texture frameTexture) {
        this.frameTexture = frameTexture;
    }
    
    /**
     * Establece la textura frontal
     */
    public void setFrontTexture(Texture frontTexture) {
        this.frontTexture = frontTexture;
    }
    
    /**
     * Actualiza la animación de la carta
     */
    public void update(float delta) {
        // Animación de flip
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
        
        // Animación de brillo/pulso cuando está matched
        if (matched) {
            // Efecto de pulso de alpha
            matchGlowAlpha += matchGlowDirection * delta * 1.5f;
            
            if (matchGlowAlpha >= 0.4f) {
                matchGlowAlpha = 0.4f;
                matchGlowDirection = -1f;
            } else if (matchGlowAlpha <= 0.1f) {
                matchGlowAlpha = 0.1f;
                matchGlowDirection = 1f;
            }
            
            // Efecto de escala inicial al hacer match
            if (matchScaleTimer < 0.3f) {
                matchScaleTimer += delta;
                // Efecto de "pop" y luego volver a normal
                if (matchScaleTimer < 0.15f) {
                    matchScaleEffect = 1f + (matchScaleTimer / 0.15f) * 0.1f; // Crece a 1.1
                } else {
                    matchScaleEffect = 1.1f - ((matchScaleTimer - 0.15f) / 0.15f) * 0.1f; // Vuelve a 1.0
                }
            } else {
                matchScaleEffect = 1f;
            }
        }
    }
    
    /**
     * Dibuja la carta usando SpriteBatch
     */
    public void draw(SpriteBatch batch) {
        // Calcular escala para efecto 3D de flip
        float flipScaleX = Math.abs(flipProgress - 0.5f) * 2f;
        if (flipScaleX < 0.05f) flipScaleX = 0.05f;
        
        // Aplicar efecto de escala de match
        float totalScale = flipScaleX * matchScaleEffect;
        
        // Calcular dimensiones con escala
        float drawWidth = width * totalScale;
        float drawHeight = height * matchScaleEffect;
        float offsetX = (width - drawWidth) / 2;
        float offsetY = (height - drawHeight) / 2;
        
        // Determinar qué lado mostrar
        boolean showingFront = flipProgress >= 0.5f;
        
        if (showingFront) {
            drawFront(batch, x + offsetX, y + offsetY, drawWidth, drawHeight);
        } else {
            drawBack(batch, x + offsetX, y + offsetY, drawWidth, drawHeight);
        }
    }
    
    /**
     * Dibuja el frente de la carta (personaje + marco)
     */
    private void drawFront(SpriteBatch batch, float drawX, float drawY, float drawWidth, float drawHeight) {
        // Ajustar color/alpha si está matched
        if (matched) {
            float alpha = 0.6f + matchGlowAlpha;
            batch.setColor(1, 1, 1, alpha);
        }
        
        // 1. Dibujar imagen del personaje (capa base)
        if (frontTexture != null) {
            batch.draw(frontTexture, drawX, drawY, drawWidth, drawHeight);
        } else {
            // Si no hay textura, dibujar un placeholder de color
            // (El ShapeRenderer se maneja aparte en GameGrid)
        }
        
        // 2. Dibujar marco encima (capa superior)
        if (frameTexture != null) {
            batch.draw(frameTexture, drawX, drawY, drawWidth, drawHeight);
        }
        
        // Restaurar color
        batch.setColor(1, 1, 1, 1);
    }
    
    /**
     * Dibuja el reverso de la carta
     */
    private void drawBack(SpriteBatch batch, float drawX, float drawY, float drawWidth, float drawHeight) {
        if (backTexture != null) {
            batch.draw(backTexture, drawX, drawY, drawWidth, drawHeight);
        }
    }
    
    /**
     * Dibuja un placeholder cuando no hay texturas
     * Usar con ShapeRenderer ANTES de batch.begin()
     */
    public void drawPlaceholder(ShapeRenderer shapeRenderer, boolean isFront) {
        float flipScaleX = Math.abs(flipProgress - 0.5f) * 2f;
        if (flipScaleX < 0.05f) flipScaleX = 0.05f;
        
        float drawWidth = width * flipScaleX * matchScaleEffect;
        float drawHeight = height * matchScaleEffect;
        float offsetX = (width - drawWidth) / 2;
        float offsetY = (height - drawHeight) / 2;
        
        if (isFront) {
            // Frente - color según matched
            if (matched) {
                shapeRenderer.setColor(0.3f, 0.7f, 0.4f, 0.8f);
            } else {
                shapeRenderer.setColor(0.4f, 0.4f, 0.6f, 1f);
            }
        } else {
            // Reverso
            shapeRenderer.setColor(0.2f, 0.2f, 0.35f, 1f);
        }
        
        shapeRenderer.rect(x + offsetX, y + offsetY, drawWidth, drawHeight);
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
    
    // ===== GETTERS Y SETTERS =====
    
    public int getCharacterId() {
        return characterId;
    }
    
    public int getRarity() {
        return rarity;
    }
    
    public void setRarity(int rarity) {
        this.rarity = rarity;
    }
    
    public boolean isRevealed() {
        return revealed;
    }
    
    public boolean isMatched() {
        return matched;
    }
    
    public void setMatched(boolean matched) {
        this.matched = matched;
        if (matched) {
            // Iniciar animación de match
            matchGlowAlpha = 0.1f;
            matchGlowDirection = 1f;
            matchScaleEffect = 1f;
            matchScaleTimer = 0f;
        }
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
    
    public Texture getFrontTexture() {
        return frontTexture;
    }
    
    public Texture getBackTexture() {
        return backTexture;
    }
    
    public Texture getFrameTexture() {
        return frameTexture;
    }
    
    public boolean hasFrontTexture() {
        return frontTexture != null;
    }
    
    public boolean hasBackTexture() {
        return backTexture != null;
    }
    
    public boolean hasFrameTexture() {
        return frameTexture != null;
    }
}