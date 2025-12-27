package com.waifu.memory.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.waifu.memory.utils.Constants;

public class Card {

    private float x, y;
    private float width, height;
    private final Rectangle bounds;

    private final int characterId;
    private final int rarity;

    private int gridIndex;

    private Texture frontTexture;
    private Texture backTexture;
    private Texture frameTexture;

    private boolean revealed;
    private boolean matched;
    private boolean animating;

    private float flipProgress;
    private float flipDirection;
    private final float flipSpeed;

    private float matchGlowAlpha;
    private float matchGlowDirection;
    private float matchScaleEffect;
    private float matchScaleTimer;

    private float shakeTimeLeft;
    private float shakeDuration;
    private float shakeAmplitude;
    private float shakeElapsed;

    private static final float MATCH_GLOW_SPEED = 1.5f;
    private static final float MATCH_GLOW_MIN = 0.1f;
    private static final float MATCH_GLOW_MAX = 0.4f;
    private static final float MATCH_SCALE_DURATION = 0.3f;
    private static final float MATCH_SCALE_PEAK = 1.1f;
    private static final float MIN_FLIP_SCALE = 0.05f;

    private static final float SHAKE_FREQUENCY = 40f;

    public Card(int characterId, int rarity, Texture frontTexture, Texture backTexture, Texture frameTexture) {
        this.characterId = characterId;
        this.rarity = rarity;
        this.frontTexture = frontTexture;
        this.backTexture = backTexture;
        this.frameTexture = frameTexture;

        this.width = Constants.CARD_WORLD_WIDTH;
        this.height = Constants.CARD_WORLD_HEIGHT;
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

        this.shakeTimeLeft = 0f;
        this.shakeDuration = 0f;
        this.shakeAmplitude = 0f;
        this.shakeElapsed = 0f;
    }

    public Card(int characterId, Texture frontTexture, Texture backTexture) {
        this(characterId, 0, frontTexture, backTexture, null);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        bounds.set(x, y, width, height);
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        bounds.set(x, y, width, height);
    }

    public void setFrameTexture(Texture frameTexture) {
        this.frameTexture = frameTexture;
    }

    public void setFrontTexture(Texture frontTexture) {
        this.frontTexture = frontTexture;
    }

    public void setBackTexture(Texture backTexture) {
        this.backTexture = backTexture;
    }

    public void update(float delta) {
        updateFlipAnimation(delta);
        updateMatchAnimation(delta);
        updateShake(delta);
    }

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

    private void updateMatchAnimation(float delta) {
        if (!matched) {
            matchScaleEffect = 1f;
            return;
        }

        matchGlowAlpha += matchGlowDirection * delta * MATCH_GLOW_SPEED;

        if (matchGlowAlpha >= MATCH_GLOW_MAX) {
            matchGlowAlpha = MATCH_GLOW_MAX;
            matchGlowDirection = -1f;
        } else if (matchGlowAlpha <= MATCH_GLOW_MIN) {
            matchGlowAlpha = MATCH_GLOW_MIN;
            matchGlowDirection = 1f;
        }

        if (matchScaleTimer < MATCH_SCALE_DURATION) {
            matchScaleTimer += delta;
            float halfDuration = MATCH_SCALE_DURATION / 2f;

            if (matchScaleTimer < halfDuration) {
                float progress = matchScaleTimer / halfDuration;
                matchScaleEffect = 1f + (MATCH_SCALE_PEAK - 1f) * progress;
            } else {
                float progress = (matchScaleTimer - halfDuration) / halfDuration;
                matchScaleEffect = MATCH_SCALE_PEAK - (MATCH_SCALE_PEAK - 1f) * progress;
            }
        } else {
            matchScaleEffect = 1f;
        }
    }

    private void updateShake(float delta) {
        if (shakeTimeLeft <= 0f) return;
        shakeTimeLeft -= delta;
        shakeElapsed += delta;
        if (shakeTimeLeft <= 0f) {
            shakeTimeLeft = 0f;
            shakeDuration = 0f;
            shakeAmplitude = 0f;
            shakeElapsed = 0f;
        }
    }

    public void draw(SpriteBatch batch) {
        float flipScaleX = Math.abs(flipProgress - 0.5f) * 2f;
        if (flipScaleX < MIN_FLIP_SCALE) flipScaleX = MIN_FLIP_SCALE;

        float totalScaleX = flipScaleX * matchScaleEffect;
        float totalScaleY = matchScaleEffect;

        float drawWidth = width * totalScaleX;
        float drawHeight = height * totalScaleY;
        float offsetX = (width - drawWidth) / 2f;
        float offsetY = (height - drawHeight) / 2f;

        float sx = getShakeOffsetX();
        float sy = getShakeOffsetY();

        boolean showingFront = flipProgress >= 0.5f;

        if (showingFront) {
            drawFront(batch, x + offsetX + sx, y + offsetY + sy, drawWidth, drawHeight);
        } else {
            drawBack(batch, x + offsetX + sx, y + offsetY + sy, drawWidth, drawHeight);
        }
    }

    private void drawFront(SpriteBatch batch, float drawX, float drawY, float drawWidth, float drawHeight) {
        if (matched) {
            float alpha = 0.6f + matchGlowAlpha;
            batch.setColor(1f, 1f, 1f, alpha);
        }

        if (frontTexture != null) {
            batch.draw(frontTexture, drawX, drawY, drawWidth, drawHeight);
        }

        if (frameTexture != null) {
            batch.draw(frameTexture, drawX, drawY, drawWidth, drawHeight);
        }

        if (matched) {
            batch.setColor(1f, 1f, 1f, 1f);
        }
    }

    private void drawBack(SpriteBatch batch, float drawX, float drawY, float drawWidth, float drawHeight) {
        if (backTexture != null) {
            batch.draw(backTexture, drawX, drawY, drawWidth, drawHeight);
        }
    }

    public void drawPlaceholder(ShapeRenderer shapeRenderer, boolean isFront) {
        float flipScaleX = Math.abs(flipProgress - 0.5f) * 2f;
        if (flipScaleX < MIN_FLIP_SCALE) flipScaleX = MIN_FLIP_SCALE;

        float totalScaleX = flipScaleX * matchScaleEffect;
        float totalScaleY = matchScaleEffect;

        float drawWidth = width * totalScaleX;
        float drawHeight = height * totalScaleY;
        float offsetX = (width - drawWidth) / 2f;
        float offsetY = (height - drawHeight) / 2f;

        float sx = getShakeOffsetX();
        float sy = getShakeOffsetY();

        if (isFront) {
            if (matched) shapeRenderer.setColor(0.3f, 0.7f, 0.4f, 0.8f);
            else shapeRenderer.setColor(0.4f, 0.4f, 0.6f, 1f);
        } else {
            shapeRenderer.setColor(0.2f, 0.2f, 0.35f, 1f);
        }

        shapeRenderer.rect(x + offsetX + sx, y + offsetY + sy, drawWidth, drawHeight);
    }

    public void flip() {
        if (!revealed && !animating && !matched) {
            animating = true;
            flipDirection = 1f;
        }
    }

    public void flipBack() {
        if (revealed && !animating && !matched) {
            animating = true;
            flipDirection = -1f;
        }
    }

    public void revealInstant() {
        revealed = true;
        flipProgress = 1f;
        animating = false;
    }

    public void hideInstant() {
        revealed = false;
        flipProgress = 0f;
        animating = false;
    }

    public boolean contains(float px, float py) {
        return bounds.contains(px, py);
    }

    public void triggerShake(float durationSeconds, float amplitudeWorldUnits) {
        if (durationSeconds <= 0f || amplitudeWorldUnits <= 0f) return;
        shakeDuration = durationSeconds;
        shakeTimeLeft = durationSeconds;
        shakeAmplitude = amplitudeWorldUnits;
        shakeElapsed = 0f;
    }

    private float getShakeOffsetX() {
        if (shakeTimeLeft <= 0f || shakeDuration <= 0f) return 0f;
        float t = shakeElapsed;
        float fade = shakeTimeLeft / shakeDuration;
        return MathUtils.sin(t * SHAKE_FREQUENCY) * shakeAmplitude * fade;
    }

    private float getShakeOffsetY() {
        if (shakeTimeLeft <= 0f || shakeDuration <= 0f) return 0f;
        float t = shakeElapsed;
        float fade = shakeTimeLeft / shakeDuration;
        return MathUtils.cos(t * (SHAKE_FREQUENCY * 0.85f)) * (shakeAmplitude * 0.35f) * fade;
    }

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

    public boolean hasFrontTexture() {
        return frontTexture != null;
    }

    public boolean hasBackTexture() {
        return backTexture != null;
    }

    public boolean hasFrameTexture() {
        return frameTexture != null;
    }

    public void setGridIndex(int index) {
        this.gridIndex = index;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
        if (matched) {
            matchGlowAlpha = MATCH_GLOW_MIN;
            matchGlowDirection = 1f;
            matchScaleEffect = 1f;
            matchScaleTimer = 0f;
        } else {
            matchGlowAlpha = 0f;
            matchScaleEffect = 1f;
            matchScaleTimer = 0f;
        }
    }

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

        shakeTimeLeft = 0f;
        shakeDuration = 0f;
        shakeAmplitude = 0f;
        shakeElapsed = 0f;
    }
}