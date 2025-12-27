package com.waifu.memory.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

public class PlaceholderTextures implements Disposable {

    private Texture character;
    private Texture cardBack;
    private Texture[] frames;

    private boolean initialized = false;

    public void init(int size, int variants) {
        int width = size;
        int height = (Constants.ASSET_SIZE_CHARACTERS_H > 0) ? Constants.ASSET_SIZE_CHARACTERS_H : size;
        init(width, height, variants);
    }

    public void init(int width, int height, int variants) {
        if (initialized) return;

        int vCount = Math.max(1, variants);
        frames = new Texture[vCount];

        character = createCharacterPlaceholder(width, height);
        cardBack = createCardBackPlaceholder(width, height);

        for (int i = 0; i < vCount; i++) {
            Color c = getFrameColor(i);
            frames[i] = createFramePlaceholder(width, height, c, i);
        }

        initialized = true;
    }

    public Texture getCharacter() {
        return character;
    }

    public Texture getCardBack() {
        return cardBack;
    }

    public Texture getFrame(int variant) {
        if (frames == null || frames.length == 0) return character;
        int v = variant;
        if (v < 0 || v >= frames.length) v = 0;
        return frames[v];
    }

    private Texture createCharacterPlaceholder(int width, int height) {
        Pixmap p = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        p.setColor(new Color(0.30f, 0.30f, 0.40f, 1f));
        p.fill();

        drawDiagonalPattern(p, width, height, new Color(1f, 1f, 1f, 0.10f), 18);

        p.setColor(new Color(1f, 1f, 1f, 0.18f));
        drawSimpleSilhouette(p, width, height);

        drawBorder(p, width, height, Color.WHITE);

        Texture t = new Texture(p);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        p.dispose();
        return t;
    }

    private Texture createCardBackPlaceholder(int width, int height) {
        Pixmap p = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        p.setColor(new Color(0.12f, 0.12f, 0.22f, 1f));
        p.fill();

        drawDiagonalPattern(p, width, height, new Color(1f, 1f, 1f, 0.08f), 16);

        p.setColor(new Color(1f, 1f, 1f, 0.12f));
        drawDiamondPattern(p, width, height);

        drawBorder(p, width, height, Color.WHITE);

        Texture t = new Texture(p);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        p.dispose();
        return t;
    }

    private Texture createFramePlaceholder(int width, int height, Color color, int variant) {
        Pixmap p = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        p.setColor(0f, 0f, 0f, 0f);
        p.fill();

        int border = Math.max(14, width / 28);
        int innerBorder = Math.max(6, border / 2);

        p.setColor(color);
        p.fillRectangle(0, 0, width, border);
        p.fillRectangle(0, height - border, width, border);
        p.fillRectangle(0, 0, border, height);
        p.fillRectangle(width - border, 0, border, height);

        p.setColor(new Color(1f, 1f, 1f, 0.55f));
        p.drawRectangle(innerBorder, innerBorder, width - innerBorder * 2, height - innerBorder * 2);

        p.setColor(Color.WHITE);
        int corner = Math.max(20, width / 18);
        drawCorner(p, 0, 0, corner);
        drawCorner(p, width - corner, 0, corner);
        drawCorner(p, 0, height - corner, corner);
        drawCorner(p, width - corner, height - corner, corner);

        drawStars(p, width, height, variant);

        Texture t = new Texture(p);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        p.dispose();
        return t;
    }

    private void drawBorder(Pixmap p, int width, int height, Color color) {
        p.setColor(color);
        p.drawRectangle(0, 0, width, height);
        p.drawRectangle(1, 1, width - 2, height - 2);
    }

    private void drawDiagonalPattern(Pixmap p, int width, int height, Color color, int step) {
        p.setColor(color);
        int max = width + height;
        for (int i = -height; i < max; i += step) {
            p.drawLine(i, 0, i + height, height);
        }
    }

    private void drawDiamondPattern(Pixmap p, int width, int height) {
        int cx = width / 2;
        int cy = height / 2;

        int size = Math.min(width, height) / 6;
        for (int y = -2; y <= 2; y++) {
            for (int x = -2; x <= 2; x++) {
                int dx = cx + x * size * 2;
                int dy = cy + y * size * 2;
                drawDiamond(p, dx, dy, size);
            }
        }
    }

    private void drawDiamond(Pixmap p, int cx, int cy, int size) {
        p.drawLine(cx, cy + size, cx + size, cy);
        p.drawLine(cx + size, cy, cx, cy - size);
        p.drawLine(cx, cy - size, cx - size, cy);
        p.drawLine(cx - size, cy, cx, cy + size);
    }

    private void drawSimpleSilhouette(Pixmap p, int width, int height) {
        int cx = width / 2;

        int headR = Math.max(18, width / 10);
        int headY = Math.max(headR + 12, height / 6);

        int bodyW = Math.max(50, width / 3);
        int bodyH = Math.max(120, height / 2);

        int bodyX = cx - bodyW / 2;
        int bodyY = headY + headR - 10;

        p.fillCircle(cx, headY, headR);
        p.fillRectangle(bodyX, bodyY, bodyW, bodyH);

        int legW = Math.max(18, bodyW / 4);
        int legH = Math.max(80, height / 5);
        int legGap = Math.max(8, legW / 2);

        int legY = bodyY + bodyH;
        p.fillRectangle(cx - legGap - legW, legY, legW, legH);
        p.fillRectangle(cx + legGap, legY, legW, legH);
    }

    private void drawCorner(Pixmap p, int x, int y, int size) {
        int thickness = Math.max(3, size / 8);
        p.fillRectangle(x, y, size, thickness);
        p.fillRectangle(x, y, thickness, size);
    }

    private void drawStars(Pixmap p, int width, int height, int variant) {
        if (variant <= 0) return;

        int starCount = Math.min(variant, 3);
        int bandHeight = Math.max(24, height / 14);
        int y = height - bandHeight + (bandHeight / 2) - 6;

        int starSize = Math.max(10, width / 20);
        int spacing = starSize + Math.max(8, starSize / 2);
        int totalW = starCount * starSize + (starCount - 1) * (spacing - starSize);
        int startX = (width - totalW) / 2;

        p.setColor(Color.WHITE);
        for (int i = 0; i < starCount; i++) {
            int sx = startX + i * spacing;
            p.fillRectangle(sx, y, starSize, starSize);
        }
    }

    private Color getFrameColor(int variant) {
        switch (variant) {
            case 1: return new Color(0.70f, 0.45f, 0.20f, 1f);
            case 2: return new Color(0.70f, 0.70f, 0.75f, 1f);
            case 3: return new Color(0.90f, 0.75f, 0.10f, 1f);
            default: return new Color(0.40f, 0.40f, 0.45f, 1f);
        }
    }

    @Override
    public void dispose() {
        if (character != null) character.dispose();
        if (cardBack != null) cardBack.dispose();
        if (frames != null) {
            for (Texture t : frames) {
                if (t != null) t.dispose();
            }
        }
        initialized = false;
    }
}