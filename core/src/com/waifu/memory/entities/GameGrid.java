package com.waifu.memory.entities;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.waifu.memory.screens.GameScreen;
import com.waifu.memory.utils.Constants;

public class GameGrid {

    private final int gridSize;
    private final int totalCards;
    private final Card[] cards;

    private final GameScreen gameScreen;
    private final int[] characterPool;

    private float gridX, gridY;
    private float cardW, cardH;
    private float spacing;

    private final Texture cardBackTexture;
    private final Texture baseFrameTexture;

    private static final float HORIZONTAL_PADDING = 80f;
    private static final float VERTICAL_PADDING = 320f;
    private static final float GRID_Y_OFFSET = -40f;
    private static final float BACKGROUND_PADDING = 20f;

    private static final float HINT_SHAKE_DURATION = 0.7f;
    private static final float HINT_SHAKE_AMPLITUDE = 8f;

    public GameGrid(int size, GameScreen gameScreen, int[] characterPool) {
        this.gridSize = size;
        this.gameScreen = gameScreen;
        this.totalCards = size * size;
        this.cards = new Card[totalCards];
        this.characterPool = (characterPool != null && characterPool.length > 0) ? characterPool : null;

        calculateLayout();

        this.cardBackTexture = gameScreen.getAssetManager().getCardBackTexture();
        this.baseFrameTexture = gameScreen.getAssetManager().getFrameTexture(0);

        createCards();
    }

    public GameGrid(int size, GameScreen gameScreen) {
        this(size, gameScreen, null);
    }

    private void calculateLayout() {
        float availableWidth = Constants.WORLD_WIDTH - HORIZONTAL_PADDING;
        float availableHeight = Constants.WORLD_HEIGHT - VERTICAL_PADDING;

        float padding = Constants.CARD_PADDING;
        float ratio = Constants.CARD_ASPECT_RATIO;

        float maxW = (availableWidth - (gridSize - 1) * padding) / gridSize;
        float maxH = maxW / ratio;

        float totalH = gridSize * maxH + (gridSize - 1) * padding;
        if (totalH > availableHeight) {
            maxH = (availableHeight - (gridSize - 1) * padding) / gridSize;
            maxW = maxH * ratio;
        }

        cardW = maxW;
        cardH = maxH;
        spacing = padding;

        float gridWidth = gridSize * cardW + (gridSize - 1) * spacing;
        float gridHeight = gridSize * cardH + (gridSize - 1) * spacing;

        gridX = (Constants.WORLD_WIDTH - gridWidth) / 2f;
        gridY = (Constants.WORLD_HEIGHT - gridHeight) / 2f + GRID_Y_OFFSET;
    }

    private void createCards() {
        int numPairs = totalCards / 2;

        int[] ids = new int[totalCards];
        for (int i = 0; i < numPairs; i++) {
            int charId;
            if (characterPool != null) {
                charId = characterPool[i % characterPool.length];
            } else {
                charId = i % Constants.TOTAL_CHARACTERS;
            }
            ids[i * 2] = charId;
            ids[i * 2 + 1] = charId;
        }

        shuffleArray(ids);

        for (int i = 0; i < totalCards; i++) {
            int charId = ids[i];

            Texture frontTexture = gameScreen.getAssetManager().getCharacterTexture(charId, 0);

            Card card = new Card(charId, 0, frontTexture, cardBackTexture, baseFrameTexture);
            card.setGridIndex(i);

            int row = i / gridSize;
            int col = i % gridSize;

            float x = gridX + col * (cardW + spacing);
            float y = gridY + (gridSize - 1 - row) * (cardH + spacing);

            card.setPosition(x, y);
            card.setSize(cardW, cardH);

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

    public void shuffleUnmatched() {
        Array<Integer> unmatched = new Array<>();
        for (int i = 0; i < totalCards; i++) {
            Card c = cards[i];
            if (c != null && !c.isMatched()) unmatched.add(i);
        }
        if (unmatched.size <= 1) return;

        float[] ox = new float[unmatched.size];
        float[] oy = new float[unmatched.size];

        for (int i = 0; i < unmatched.size; i++) {
            int idx = unmatched.get(i);
            ox[i] = cards[idx].getX();
            oy[i] = cards[idx].getY();
        }

        for (int i = unmatched.size - 1; i > 0; i--) {
            int j = MathUtils.random(i);

            float tx = ox[i];
            ox[i] = ox[j];
            ox[j] = tx;

            float ty = oy[i];
            oy[i] = oy[j];
            oy[j] = ty;
        }

        for (int i = 0; i < unmatched.size; i++) {
            int idx = unmatched.get(i);
            cards[idx].setPosition(ox[i], oy[i]);

            if (cards[idx].isRevealed() && !cards[idx].isMatched()) {
                cards[idx].hideInstant();
            }
        }
    }

    public void update(float delta) {
        for (Card card : cards) {
            if (card != null) card.update(delta);
        }
    }

    public void draw(SpriteBatch batch, ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        float gridWidth = gridSize * cardW + (gridSize - 1) * spacing;
        float gridHeight = gridSize * cardH + (gridSize - 1) * spacing;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.08f, 0.08f, 0.12f, 0.7f);
        shapeRenderer.rect(
            gridX - BACKGROUND_PADDING,
            gridY - BACKGROUND_PADDING,
            gridWidth + BACKGROUND_PADDING * 2,
            gridHeight + BACKGROUND_PADDING * 2
        );
        shapeRenderer.end();

        batch.begin();
        for (Card card : cards) {
            if (card != null) card.draw(batch);
        }
        batch.end();
    }

    public Card getCardAt(float x, float y) {
        for (Card card : cards) {
            if (card != null && card.contains(x, y)) return card;
        }
        return null;
    }

    public boolean isAllMatched() {
        for (Card card : cards) {
            if (card != null && !card.isMatched()) return false;
        }
        return true;
    }

    public int getTotalPairs() {
        return totalCards / 2;
    }

    public int getGridSize() {
        return gridSize;
    }

    public boolean triggerHintShake() {
        Array<Card> candidates = new Array<>();
        for (Card c : cards) {
            if (c == null) continue;
            if (c.isMatched()) continue;
            if (c.isRevealed()) continue;
            if (c.isAnimating()) continue;
            candidates.add(c);
        }
        if (candidates.size < Constants.HINT_SHAKE_TOTAL_CARDS) return false;

        IntMap<Array<Card>> byId = new IntMap<>();
        for (Card c : candidates) {
            Array<Card> list = byId.get(c.getCharacterId());
            if (list == null) {
                list = new Array<>();
                byId.put(c.getCharacterId(), list);
            }
            list.add(c);
        }

        Array<Integer> pairIds = new Array<>();
        for (IntMap.Entry<Array<Card>> e : byId.entries()) {
            if (e.value != null && e.value.size >= 2) pairIds.add(e.key);
        }
        if (pairIds.size == 0) return false;

        int chosenId = pairIds.random();
        Array<Card> pairList = byId.get(chosenId);

        Card a = pairList.random();
        Card b = a;
        int guard = 0;
        while (b == a && guard++ < 20) b = pairList.random();
        if (a == b) return false;

        Array<Card> decoyPool = new Array<>();
        for (Card c : candidates) {
            if (c == a || c == b) continue;
            if (c.getCharacterId() == chosenId) continue;
            decoyPool.add(c);
        }
        if (decoyPool.size < Constants.HINT_SHAKE_DECOY_CARDS) return false;

        Card d1 = decoyPool.random();
        Card d2 = d1;
        guard = 0;
        while ((d2 == d1 || d2.getCharacterId() == d1.getCharacterId()) && guard++ < 40) {
            d2 = decoyPool.random();
        }
        if (d2 == d1) return false;

        a.triggerShake(HINT_SHAKE_DURATION, HINT_SHAKE_AMPLITUDE);
        b.triggerShake(HINT_SHAKE_DURATION, HINT_SHAKE_AMPLITUDE);
        d1.triggerShake(HINT_SHAKE_DURATION, HINT_SHAKE_AMPLITUDE);
        d2.triggerShake(HINT_SHAKE_DURATION, HINT_SHAKE_AMPLITUDE);

        return true;
    }

    public void dispose() {
    }
}