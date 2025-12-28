private void checkMatch() {
    boolean matched = false;
    
    if (firstCard != null && secondCard != null && 
        firstCard.getCharacterId() == secondCard.getCharacterId()) {
        
        firstCard.setMatched(true);
        secondCard.setMatched(true);
        pairsFound++;
        matched = true;

        pcoinsEarned = pairsFound * getPlayerData().getCurrentPairValue();

        audioManager.playMatch();
        audioManager.playCoinCollect();

        if (gameGrid.isAllMatched()) {
            onVictory();
            return;
        }
    } else {
        if (firstCard != null) firstCard.flipBack();
        if (secondCard != null) secondCard.flipBack();
        audioManager.playNoMatch();
    }

    // Solo shuffle si NO hubo match (más justo para el jugador)
    if (!matched && shuffleEnabled && cardsFlippedSinceShuffle >= shuffleInterval) {
        gameGrid.shuffleUnmatched();
        cardsFlippedSinceShuffle = 0;
        audioManager.playShuffle(); // Añadir feedback de audio
    }

    firstCard = null;
    secondCard = null;
    gameState = GameState.PLAYING;
}