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
import com.waifu.memory.data.HdLinksDatabase;
import com.waifu.memory.utils.Constants;

public class GalleryScreen extends BaseScreen {

    private BitmapFont titleFont;
    private BitmapFont textFont;
    private BitmapFont smallFont;
    private GlyphLayout layout;

    private ShapeRenderer shapeRenderer;

    private static final int COLUMNS = 4;
    private Rectangle[] characterSlots;
    private float slotSize;

    private float scrollY;
    private float maxScrollY;
    private float lastTouchY;
    private boolean isDragging;

    private Rectangle backButton;

    private boolean viewingCharacter;
    private int viewedCharacterId;
    private int viewedVariant;
    private Rectangle closeViewerButton;
    private Rectangle prevVariantButton;
    private Rectangle nextVariantButton;
    private Rectangle unlockButton;

    private Rectangle downloadHdButton;

    private Texture currentViewTexture;
    private int currentViewCharId;
    private int currentViewVariant;

    private final HdLinksDatabase hdLinks = new HdLinksDatabase();

    private Vector3 touchPos;

    public GalleryScreen(IQWaifuMemory game) {
        super(game);

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

        scrollY = 0;
        lastTouchY = 0;
        isDragging = false;
        viewingCharacter = false;

        currentViewTexture = null;
        currentViewCharId = -1;
        currentViewVariant = -1;

        createUI();
        setupInput();
    }

    private void createUI() {
        float padding = 40f;
        float spacing = 15f;

        backButton = new Rectangle(padding, Constants.WORLD_HEIGHT - 100, 80, 80);

        slotSize = (Constants.WORLD_WIDTH - padding * 2 - (COLUMNS - 1) * spacing) / COLUMNS;
        int totalRows = (int) Math.ceil((float) Constants.TOTAL_CHARACTERS / COLUMNS);

        characterSlots = new Rectangle[Constants.TOTAL_CHARACTERS];
        float startY = Constants.WORLD_HEIGHT - 200;

        for (int i = 0; i < Constants.TOTAL_CHARACTERS; i++) {
            int row = i / COLUMNS;
            int col = i % COLUMNS;

            float x = padding + col * (slotSize + spacing);
            float y = startY - row * (slotSize + spacing);

            characterSlots[i] = new Rectangle(x, y, slotSize, slotSize);
        }

        float contentHeight = totalRows * (slotSize + spacing);
        float visibleHeight = Constants.WORLD_HEIGHT - 250;
        maxScrollY = Math.max(0, contentHeight - visibleHeight);

        float btnSize = 80f;
        closeViewerButton = new Rectangle(Constants.WORLD_WIDTH - btnSize - 40, Constants.WORLD_HEIGHT - btnSize - 40, btnSize, btnSize);
        prevVariantButton = new Rectangle(40, Constants.WORLD_HEIGHT / 2 - btnSize / 2, btnSize, btnSize);
        nextVariantButton = new Rectangle(Constants.WORLD_WIDTH - btnSize - 40, Constants.WORLD_HEIGHT / 2 - btnSize / 2, btnSize, btnSize);
        unlockButton = new Rectangle(Constants.WORLD_WIDTH / 2 - 170, 135, 340, 80);

        downloadHdButton = new Rectangle(Constants.WORLD_WIDTH / 2 - 220, 40, 440, 70);
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
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                touchPos.set(screenX, screenY, 0);
                viewport.unproject(touchPos);

                if (!isDragging) {
                    if (viewingCharacter) handleViewerClick();
                    else handleGalleryClick();
                }

                isDragging = false;
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (viewingCharacter) return false;

                touchPos.set(screenX, screenY, 0);
                viewport.unproject(touchPos);

                float deltaY = lastTouchY - touchPos.y;

                if (Math.abs(deltaY) > 5) {
                    isDragging = true;
                    scrollY = Math.max(0, Math.min(maxScrollY, scrollY + deltaY));
                }

                lastTouchY = touchPos.y;
                return true;
            }
        });
    }

    private void handleGalleryClick() {
        if (backButton.contains(touchPos.x, touchPos.y)) {
            audioManager.playButtonClick();
            goToScreen(new HomeScreen(game));
            return;
        }

        for (int i = 0; i < characterSlots.length; i++) {
            Rectangle slot = characterSlots[i];
            float adjustedY = slot.y + scrollY;

            if (touchPos.x >= slot.x && touchPos.x <= slot.x + slot.width &&
                touchPos.y >= adjustedY && touchPos.y <= adjustedY + slot.height) {
                openViewer(i);
                return;
            }
        }
    }

    private void handleViewerClick() {
        if (closeViewerButton.contains(touchPos.x, touchPos.y)) {
            audioManager.playButtonClick();
            closeViewer();
            return;
        }

        int unlockLevel = getPlayerData().getCharacterUnlockLevel(viewedCharacterId);

        if (unlockLevel > 1) {
            if (prevVariantButton.contains(touchPos.x, touchPos.y)) {
                audioManager.playButtonClick();
                changeVariant(-1);
                return;
            }

            if (nextVariantButton.contains(touchPos.x, touchPos.y)) {
                audioManager.playButtonClick();
                changeVariant(1);
                return;
            }
        }

        if (unlockButton.contains(touchPos.x, touchPos.y)) {
            tryUnlock();
            return;
        }

        if (isDownloadHdAvailable() && downloadHdButton.contains(touchPos.x, touchPos.y)) {
            audioManager.playButtonClick();
            tryDownloadHdWithAd();
        }
    }

    private void openViewer(int characterId) {
        audioManager.playButtonClick();
        viewingCharacter = true;
        viewedCharacterId = characterId;
        viewedVariant = 0;
        loadViewerTexture();
    }

    private void closeViewer() {
        viewingCharacter = false;

        assetManager.unloadCharacter(viewedCharacterId);

        currentViewTexture = null;
        currentViewCharId = -1;
        currentViewVariant = -1;
    }

    private void loadViewerTexture() {
        if (currentViewCharId == viewedCharacterId && currentViewVariant == viewedVariant) return;

        if (currentViewCharId != viewedCharacterId && currentViewCharId >= 0) {
            assetManager.unloadCharacter(currentViewCharId);
        }

        if (getPlayerData().isVariantUnlocked(viewedCharacterId, viewedVariant)) {
            currentViewTexture = assetManager.getCharacterTexture(viewedCharacterId, viewedVariant);
        } else {
            currentViewTexture = null;
        }

        currentViewCharId = viewedCharacterId;
        currentViewVariant = viewedVariant;
    }

    private void changeVariant(int direction) {
        int unlockLevel = getPlayerData().getCharacterUnlockLevel(viewedCharacterId);
        int maxVariant = Math.max(0, unlockLevel - 1);

        viewedVariant += direction;

        if (viewedVariant < 0) viewedVariant = maxVariant;
        if (viewedVariant > maxVariant) viewedVariant = 0;

        loadViewerTexture();
    }

    private void tryUnlock() {
        if (getPlayerData().unlockNextVariant(viewedCharacterId)) {
            audioManager.playButtonClick();
            audioManager.playCoinCollect();
            saveProgress();
            loadViewerTexture();
        }
    }

    private boolean isDownloadHdAvailable() {
        if (viewedVariant != Constants.HD_DOWNLOAD_VARIANT) return false;
        if (!getPlayerData().isVariantUnlocked(viewedCharacterId, Constants.HD_DOWNLOAD_VARIANT)) return false;
        return hdLinks.hasUrlFor(viewedCharacterId);
    }

    private void tryDownloadHdWithAd() {
        if (!game.hasAdHandler() || !game.getAdHandler().isRewardedAdLoaded()) return;

        game.getAdHandler().showRewardedAd(new IQWaifuMemory.RewardCallback() {
            @Override
            public void onRewardEarned() {
                getPlayerData().recordRewardedWatched();
                startHdDownload();
            }

            @Override
            public void onAdFailed() {
                Gdx.app.log(Constants.TAG, "Ad failed to show");
            }
        });
    }

    private void startHdDownload() {
        String url = hdLinks.getUrl(viewedCharacterId);
        if (url == null || url.isEmpty()) return;

        getPlayerData().recordHdDownload();
        saveProgress();

        Gdx.net.openURI(url);
    }

    @Override
    protected void update(float delta) {
    }

    @Override
    protected void draw() {
        if (viewingCharacter) drawViewer();
        else drawGrid();
    }

    private void drawGrid() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 1f);
        shapeRenderer.rect(0, Constants.WORLD_HEIGHT - 130, Constants.WORLD_WIDTH, 130);

        shapeRenderer.setColor(0.3f, 0.3f, 0.4f, 1f);
        shapeRenderer.rect(backButton.x, backButton.y, backButton.width, backButton.height);

        for (int i = 0; i < characterSlots.length; i++) {
            Rectangle slot = characterSlots[i];
            float adjustedY = slot.y + scrollY;

            if (adjustedY > -slot.height && adjustedY < Constants.WORLD_HEIGHT - 130) {
                int unlockLevel = getPlayerData().getCharacterUnlockLevel(i);
                float[] color = Constants.getRarityColor(Math.max(0, unlockLevel - 1));

                if (unlockLevel > 0) shapeRenderer.setColor(color[0] * 0.4f, color[1] * 0.4f, color[2] * 0.4f, 1f);
                else shapeRenderer.setColor(0.12f, 0.12f, 0.15f, 1f);

                shapeRenderer.rect(slot.x, adjustedY, slot.width, slot.height);
            }
        }

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < characterSlots.length; i++) {
            Rectangle slot = characterSlots[i];
            float adjustedY = slot.y + scrollY;

            if (adjustedY > -slot.height && adjustedY < Constants.WORLD_HEIGHT - 130) {
                int unlockLevel = getPlayerData().getCharacterUnlockLevel(i);
                float[] color = Constants.getRarityColor(Math.max(0, unlockLevel - 1));
                shapeRenderer.setColor(color[0], color[1], color[2], 0.8f);
                shapeRenderer.rect(slot.x, adjustedY, slot.width, slot.height);
            }
        }
        shapeRenderer.end();

        batch.begin();

        String title = "GALERIA";
        layout.setText(titleFont, title);
        titleFont.draw(batch, title, Constants.WORLD_WIDTH / 2 - layout.width / 2, Constants.WORLD_HEIGHT - 45);

        String pcoinsText = Constants.CURRENCY_NAME + ": " + getPlayerData().pcoins;
        layout.setText(textFont, pcoinsText);
        textFont.draw(batch, pcoinsText, Constants.WORLD_WIDTH - layout.width - 40, Constants.WORLD_HEIGHT - 50);

        String progress = String.format("%.1f%%", getPlayerData().getGalleryCompletionPercent());
        layout.setText(smallFont, progress);
        smallFont.draw(batch, progress, Constants.WORLD_WIDTH / 2 - layout.width / 2, Constants.WORLD_HEIGHT - 105);

        layout.setText(textFont, "<");
        textFont.draw(batch, "<",
            backButton.x + (backButton.width - layout.width) / 2,
            backButton.y + (backButton.height + layout.height) / 2);

        for (int i = 0; i < characterSlots.length; i++) {
            Rectangle slot = characterSlots[i];
            float adjustedY = slot.y + scrollY;

            if (adjustedY > -slot.height && adjustedY < Constants.WORLD_HEIGHT - 130) {
                int unlockLevel = getPlayerData().getCharacterUnlockLevel(i);

                String num = String.format("#%02d", i + 1);
                layout.setText(smallFont, num);
                smallFont.setColor(Color.WHITE);
                smallFont.draw(batch, num, slot.x + 5, adjustedY + slot.height - 5);

                if (unlockLevel == 0) {
                    smallFont.setColor(Color.GRAY);
                    int cost = Constants.getGalleryCost(0);
                    String costStr = cost + "P";
                    layout.setText(smallFont, costStr);
                    smallFont.draw(batch, costStr, slot.x + (slot.width - layout.width) / 2, adjustedY + slot.height / 2);
                } else {
                    String stars = Constants.getVariantDisplayName(unlockLevel - 1);
                    if (unlockLevel >= Constants.VARIANTS_PER_CHARACTER) {
                        smallFont.setColor(Color.GOLD);
                        stars = "MAX";
                    } else {
                        smallFont.setColor(Color.WHITE);
                    }
                    layout.setText(smallFont, stars);
                    smallFont.draw(batch, stars, slot.x + (slot.width - layout.width) / 2, adjustedY + 25);
                }

                smallFont.setColor(Color.WHITE);
            }
        }

        batch.end();
    }

    private void drawViewer() {
        int unlockLevel = getPlayerData().getCharacterUnlockLevel(viewedCharacterId);
        boolean isUnlocked = getPlayerData().isVariantUnlocked(viewedCharacterId, viewedVariant);

        float panelW = Constants.WORLD_WIDTH - 160f;
        float panelH = panelW / Constants.CARD_ASPECT_RATIO;
        float maxH = Constants.WORLD_HEIGHT * 0.62f;
        if (panelH > maxH) {
            panelH = maxH;
            panelW = panelH * Constants.CARD_ASPECT_RATIO;
        }

        float panelX = (Constants.WORLD_WIDTH - panelW) / 2f;
        float panelY = Constants.WORLD_HEIGHT / 2f - panelH / 2f + 40f;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.08f, 0.08f, 0.12f, 1f);
        shapeRenderer.rect(0, 0, Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT);

        shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 1f);
        shapeRenderer.rect(panelX, panelY, panelW, panelH);

        shapeRenderer.setColor(0.6f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(closeViewerButton.x, closeViewerButton.y, closeViewerButton.width, closeViewerButton.height);

        if (unlockLevel > 1) {
            shapeRenderer.setColor(0.3f, 0.3f, 0.5f, 1f);
            shapeRenderer.rect(prevVariantButton.x, prevVariantButton.y, prevVariantButton.width, prevVariantButton.height);
            shapeRenderer.rect(nextVariantButton.x, nextVariantButton.y, nextVariantButton.width, nextVariantButton.height);
        }

        if (unlockLevel < Constants.VARIANTS_PER_CHARACTER) {
            int cost = getPlayerData().getNextUnlockCost(viewedCharacterId);
            boolean canAfford = getPlayerData().pcoins >= cost;

            if (canAfford) shapeRenderer.setColor(Constants.COLOR_PRIMARY[0], Constants.COLOR_PRIMARY[1], Constants.COLOR_PRIMARY[2], 1f);
            else shapeRenderer.setColor(0.25f, 0.25f, 0.3f, 1f);

            shapeRenderer.rect(unlockButton.x, unlockButton.y, unlockButton.width, unlockButton.height);
        }

        if (isDownloadHdAvailable()) {
            shapeRenderer.setColor(0.2f, 0.55f, 0.9f, 1f);
            shapeRenderer.rect(downloadHdButton.x, downloadHdButton.y, downloadHdButton.width, downloadHdButton.height);
        }

        shapeRenderer.end();

        batch.begin();

        float imgMargin = 15f;
        float imgW = panelW - imgMargin * 2f;
        float imgH = panelH - imgMargin * 2f;
        float imgX = panelX + imgMargin;
        float imgY = panelY + imgMargin;

        if (isUnlocked && currentViewTexture != null) {
            batch.draw(currentViewTexture, imgX, imgY, imgW, imgH);

            Texture frame = assetManager.getFrameTexture(viewedVariant);
            if (frame != null) batch.draw(frame, imgX, imgY, imgW, imgH);
        } else {
            titleFont.setColor(Color.DARK_GRAY);
            String locked = "BLOQUEADO";
            layout.setText(titleFont, locked);
            titleFont.draw(batch, locked, Constants.WORLD_WIDTH / 2 - layout.width / 2, Constants.WORLD_HEIGHT / 2);
            titleFont.setColor(Color.WHITE);
        }

        String name = "Personaje #" + (viewedCharacterId + 1);
        layout.setText(titleFont, name);
        titleFont.draw(batch, name, Constants.WORLD_WIDTH / 2 - layout.width / 2, Constants.WORLD_HEIGHT - 50);

        String varName = Constants.getVariantDisplayName(viewedVariant);
        float[] varColor = Constants.getRarityColor(viewedVariant);
        textFont.setColor(varColor[0], varColor[1], varColor[2], 1f);
        layout.setText(textFont, varName);
        textFont.draw(batch, varName, Constants.WORLD_WIDTH / 2 - layout.width / 2, Constants.WORLD_HEIGHT - 100);
        textFont.setColor(Color.WHITE);

        String indicator = (viewedVariant + 1) + "/" + Math.max(1, unlockLevel);
        layout.setText(textFont, indicator);
        textFont.draw(batch, indicator, Constants.WORLD_WIDTH / 2 - layout.width / 2, panelY - 15);

        layout.setText(titleFont, "X");
        titleFont.draw(batch, "X",
            closeViewerButton.x + (closeViewerButton.width - layout.width) / 2,
            closeViewerButton.y + (closeViewerButton.height + layout.height) / 2);

        if (unlockLevel > 1) {
            layout.setText(titleFont, "<");
            titleFont.draw(batch, "<",
                prevVariantButton.x + (prevVariantButton.width - layout.width) / 2,
                prevVariantButton.y + (prevVariantButton.height + layout.height) / 2);

            layout.setText(titleFont, ">");
            titleFont.draw(batch, ">",
                nextVariantButton.x + (nextVariantButton.width - layout.width) / 2,
                nextVariantButton.y + (nextVariantButton.height + layout.height) / 2);
        }

        if (unlockLevel < Constants.VARIANTS_PER_CHARACTER) {
            int cost = getPlayerData().getNextUnlockCost(viewedCharacterId);
            String unlockText = "DESBLOQUEAR: " + cost + " P";
            layout.setText(textFont, unlockText);
            textFont.draw(batch, unlockText,
                unlockButton.x + (unlockButton.width - layout.width) / 2,
                unlockButton.y + (unlockButton.height + layout.height) / 2);
        } else {
            textFont.setColor(Color.GOLD);
            String complete = "¡COMPLETO!";
            layout.setText(textFont, complete);
            textFont.draw(batch, complete, Constants.WORLD_WIDTH / 2 - layout.width / 2, unlockButton.y + 40);
            textFont.setColor(Color.WHITE);
        }

        if (isDownloadHdAvailable()) {
            String dl = "VER AD + DESCARGAR HD";
            layout.setText(smallFont, dl);
            smallFont.draw(batch, dl,
                downloadHdButton.x + (downloadHdButton.width - layout.width) / 2f,
                downloadHdButton.y + (downloadHdButton.height + layout.height) / 2f);
        }

        String pcoins = getPlayerData().pcoins + " " + Constants.CURRENCY_NAME;
        layout.setText(smallFont, pcoins);
        smallFont.draw(batch, pcoins, Constants.WORLD_WIDTH - layout.width - 20, 40);

        batch.end();
    }

    @Override
    public void hide() {
        super.hide();
        if (viewingCharacter) closeViewer();
    }

    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (textFont != null) textFont.dispose();
        if (smallFont != null) smallFont.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}