package com.waifu.memory.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.waifu.memory.utils.Constants;

/**
 * Representa una carta individual en el juego.
 * Soporta sistema de marcos por rareza y animaciones de match.
 */
public class Card {

    // ========== POSICIÓN Y TAMAÑO ==========
    private float x, y;
    private float width, height;
    private final Rectangle bounds;

    // ========== IDENTIFICACIÓN ==========
    // Identificador del personaje (para matching)
    private final int characterId;

    // Rareza/variante (0=Base, 1=☆, 2=☆☆, 3=☆☆☆)
    private final int rarity;

    // Índice en el grid
    private int gridIndex;

    // ========== TEXTURAS ==========
    private Texture frontTexture;   // Imagen del personaje
    private Texture backTexture;    // Reverso de la carta
    private Texture frameTexture;   // Marco según rareza

    // ========== ESTADOS ==========
    private boolean revealed;       // Carta volteada mostrando frente
    private boolean matched;        // Par encontrado
    private boolean animating;      // En animación de flip

    // ========== ANIMACIÓN DE FLIP ==========
    private float flipProgress;     // 0 = back, 1 = front
    private float flipDirection;    // 1 = voltear a frente, -1 = voltear a reverso
    private final float flipSpeed;  // progreso/segundo

    // ========== ANIMACIÓN DE MATCH ==========
    private float matchGlowAlpha;       // Alpha del efecto glow
    private float matchGlowDirection;   // Dirección del pulso
    private float matchScaleEffect;     // Escala actual del efecto
    private float matchScaleTimer;      // Timer para efecto "pop"

    // ========== CONSTANTES DE ANIMACIÓN ==========
    private static final float MATCH_GLOW_SPEED = 1.5f;
    private static final float MATCH_GLOW_MIN = 0.1f;
    private static final float MATCH_GLOW_MAX = 0.4f;
    private static final float MATCH_SCALE_DURATION = 0.3f;
    private static final float MATCH_SCALE_PEAK = 1.1f;
    private static final float MIN_FLIP_SCALE = 0.05f;

    // ========== CONSTRUCTORES ==========

    /**
     * Constructor completo con rareza y marco
     * @param characterId ID del personaje (para matching)
     * @param rarity Rareza/variante (0=Base, 1=☆, 2=☆☆, 3=☆☆☆)
     * @param frontTexture Textura del personaje
     * @param backTexture Textura del reverso
     * @param frameTexture Textura del marco (puede ser null)
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
     * Constructor básico para gameplay (usa rarity=0 y sin marco)
     * @param characterId ID del personaje
     * @param frontTexture Textura del personaje
     * @param backTexture Textura del reverso
     */
    public Card(int characterId, Texture frontTexture, Texture backTexture) {
        this(characterId, 0, frontTexture, backTexture, null);
    }

    // ========== CONFIGURACIÓN ==========

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
     * Establece la textura del reverso
     */
    public void setBackTexture(Texture backTexture) {
        this.backTexture = backTexture;
    }

    // ========== ACTUALIZACIÓN ==========

    /**
     * Actualiza las animaciones de la carta
     * @param delta Tiempo desde el último frame
     */
    public void update(float delta) {
        updateFlipAnimation(delta);
        updateMatchAnimation(delta);
    }

    /**
     * Actualiza la animación de flip
     */
    private void updateFlipAnimation(float delta) {
        if (!animating) return;

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

    /**
     * Actualiza la animación de match (glow y escala)
     */
    private void updateMatchAnimation(float delta) {
        if (!matched) return;

        // Efecto de pulso de alpha
        matchGlowAlpha += matchGlowDirection * delta * MATCH_GLOW_SPEED;

        if (matchGlowAlpha >= MATCH_GLOW_MAX) {
            matchGlowAlpha = MATCH_GLOW_MAX;
            matchGlowDirection = -1f;
        } else if (matchGlowAlpha <= MATCH_GLOW_MIN) {
            matchGlowAlpha = MATCH_GLOW_MIN;
            matchGlowDirection = 1f;
        }

        // Efecto de escala "pop" inicial
        if (matchScaleTimer < MATCH_SCALE_DURATION) {
            matchScaleTimer += delta;
            float halfDuration = MATCH_SCALE_DURATION / 2f;

            if (matchScaleTimer < halfDuration) {
                // Crece hasta MATCH_SCALE_PEAK
                float progress = matchScaleTimer / halfDuration;
                matchScaleEffect = 1f + (MATCH_SCALE_PEAK - 1f) * progress;
            } else {
                // Vuelve a 1.0
                float progress = (matchScaleTimer - halfDuration) / halfDuration;
                matchScaleEffect = MATCH_SCALE_PEAK - (MATCH_SCALE_PEAK - 1f) * progress;
            }
        } else {
            matchScaleEffect = 1f;
        }
    }

    // ========== RENDERIZADO ==========

    /**
     * Dibuja la carta usando SpriteBatch
     */
    public void draw(SpriteBatch batch) {
        // Calcular escala para efecto 3D de flip
        float flipScaleX = Math.abs(flipProgress - 0.5f) * 2f;
        if (flipScaleX < MIN_FLIP_SCALE) flipScaleX = MIN_FLIP_SCALE;

        // Aplicar efecto de escala de match
        float totalScaleX = flipScaleX * matchScaleEffect;
        float totalScaleY = matchScaleEffect;

        // Calcular dimensiones con escala
        float drawWidth = width * totalScaleX;
        float drawHeight = height * totalScaleY;
        float offsetX = (width - drawWidth) / 2f;
        float offsetY = (height - drawHeight) / 2f;

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
        // Aplicar efecto visual si está matched
        if (matched) {
            float alpha = 0.6f + matchGlowAlpha;
            batch.setColor(1f, 1f, 1f, alpha);
        }

        // 1. Dibujar imagen del personaje (capa base)
        if (frontTexture != null) {
            batch.draw(frontTexture, drawX, drawY, drawWidth, drawHeight);
        }

        // 2. Dibujar marco encima (capa superior)
        if (frameTexture != null) {
            batch.draw(frameTexture, drawX, drawY, drawWidth, drawHeight);
        }

        // Restaurar color normal
        if (matched) {
            batch.setColor(1f, 1f, 1f, 1f);
        }
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
     * Dibuja un placeholder cuando no hay texturas (para debug/desarrollo).
     * Usar con ShapeRenderer ANTES de batch.begin()
     * @param shapeRenderer El ShapeRenderer a usar
     * @param isFront true para mostrar placeholder de frente, false para reverso
     */
    public void drawPlaceholder(ShapeRenderer shapeRenderer, boolean isFront) {
        // Calcular escala
        float flipScaleX = Math.abs(flipProgress - 0.5f) * 2f;
        if (flipScaleX < MIN_FLIP_SCALE) flipScaleX = MIN_FLIP_SCALE;

        float totalScaleX = flipScaleX * matchScaleEffect;
        float totalScaleY = matchScaleEffect;

        float drawWidth = width * totalScaleX;
        float drawHeight = height * totalScaleY;
        float offsetX = (width - drawWidth) / 2f;
        float offsetY = (height - drawHeight) / 2f;

        if (isFront) {
            // Frente - color según estado
            if (matched) {
                shapeRenderer.setColor(0.3f, 0.7f, 0.4f, 0.8f); // Verde
            } else {
                shapeRenderer.setColor(0.4f, 0.4f, 0.6f, 1f);   // Gris azulado
            }
        } else {
            // Reverso
            shapeRenderer.setColor(0.2f, 0.2f, 0.35f, 1f);      // Azul oscuro
        }

        shapeRenderer.rect(x + offsetX, y + offsetY, drawWidth, drawHeight);
    }

    // ========== CONTROL DE FLIP ==========

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
     * Revela la carta instantáneamente (para preview inicial)
     */
    public void revealInstant() {
        revealed = true;
        flipProgress = 1f;
        animating = false;
    }

    /**
     * Oculta la carta instantáneamente
     */
    public void hideInstant() {
        revealed = false;
        flipProgress = 0f;
        animating = false;
    }

    // ========== DETECCIÓN DE COLISIÓN ==========

    /**
     * Verifica si un punto está dentro de la carta
     * @param px Coordenada X del punto
     * @param py Coordenada Y del punto
     * @return true si el punto está dentro de los límites
     */
    public boolean contains(float px, float py) {
        return bounds.contains(px, py);
    }

    // ========== GETTERS ==========

    public int getCharacterId() {
        return characterId;
    }

    public int getRarity() {
        return rarity;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public boolean isMatched() {
        return matched;
    }

    public boolean isAnimating() {
        return animating;
    }

    public int getGridIndex() {
        return gridIndex;
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

    // ========== VERIFICADORES DE TEXTURA ==========

    public boolean hasFrontTexture() {
        return frontTexture != null;
    }

    public boolean hasBackTexture() {
        return backTexture != null;
    }

    public boolean hasFrameTexture() {
        return frameTexture != null;
    }

    // ========== SETTERS ==========

    public void setGridIndex(int index) {
        this.gridIndex = index;
    }

    /**
     * Establece el estado de matched e inicia la animación correspondiente
     */
    public void setMatched(boolean matched) {
        this.matched = matched;
        if (matched) {
            // Iniciar animación de match
            matchGlowAlpha = MATCH_GLOW_MIN;
            matchGlowDirection = 1f;
            matchScaleEffect = 1f;
            matchScaleTimer = 0f;
        } else {
            // Resetear animación
            matchGlowAlpha = 0f;
            matchScaleEffect = 1f;
            matchScaleTimer = 0f;
        }
    }

    /**
     * Resetea la carta a su estado inicial
     */
    public void reset() {
        revealed = false;
        matched = false;
        animating = false;
        flipProgress = 0f;
        flipDirection = 0f;
        matchGlowAlpha = 0f;
        matchGlowDirection = 1f;
        matchScaleEffect = 1f;
        matchScaleTimer = 0f;
    }
}