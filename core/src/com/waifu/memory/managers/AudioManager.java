package com.waifu.memory.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.waifu.memory.utils.Constants;

/**
 * Manager para controlar música y efectos de sonido
 */
public class AudioManager implements Disposable {
    
    // Música actual reproduciéndose
    private Music currentMusic;
    private String currentMusicPath;
    
    // Cache de sonidos cargados
    private ObjectMap<String, Sound> soundCache;
    
    // Volúmenes
    private float musicVolume;
    private float sfxVolume;
    
    // Estado
    private boolean musicEnabled;
    private boolean sfxEnabled;
    
    public AudioManager() {
        soundCache = new ObjectMap<>();
        musicVolume = Constants.DEFAULT_MUSIC_VOLUME;
        sfxVolume = Constants.DEFAULT_SFX_VOLUME;
        musicEnabled = true;
        sfxEnabled = true;
    }
    
    // ========== MÚSICA ==========
    
    /**
     * Reproduce música de fondo
     * @param path Ruta del archivo de música
     * @param loop Si debe repetirse
     */
    public void playMusic(String path, boolean loop) {
        if (!musicEnabled) return;
        
        // Si ya está reproduciendo la misma música, no hacer nada
        if (currentMusicPath != null && currentMusicPath.equals(path)) {
            return;
        }
        
        // Detener música actual
        stopMusic();
        
        try {
            currentMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
            currentMusic.setLooping(loop);
            currentMusic.setVolume(musicVolume);
            currentMusic.play();
            currentMusicPath = path;
            Gdx.app.log(Constants.TAG, "Reproduciendo música: " + path);
        } catch (Exception e) {
            Gdx.app.error(Constants.TAG, "Error cargando música: " + path);
        }
    }
    
    /**
     * Detiene la música actual
     */
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
            currentMusicPath = null;
        }
    }
    
    /**
     * Pausa la música
     */
    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
    }
    
    /**
     * Reanuda la música
     */
    public void resumeMusic() {
        if (currentMusic != null && !currentMusic.isPlaying() && musicEnabled) {
            currentMusic.play();
        }
    }
    
    /**
     * Establece el volumen de la música
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
        }
    }
    
    public float getMusicVolume() {
        return musicVolume;
    }
    
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            pauseMusic();
        } else {
            resumeMusic();
        }
    }
    
    // ========== EFECTOS DE SONIDO ==========
    
    /**
     * Reproduce un efecto de sonido
     * @param path Ruta del archivo de sonido
     */
    public void playSound(String path) {
        if (!sfxEnabled) return;
        
        try {
            Sound sound = getSound(path);
            if (sound != null) {
                sound.play(sfxVolume);
            }
        } catch (Exception e) {
            Gdx.app.error(Constants.TAG, "Error reproduciendo sonido: " + path);
        }
    }
    
    /**
     * Reproduce un efecto de sonido con pitch alterado
     */
    public void playSound(String path, float pitch) {
        if (!sfxEnabled) return;
        
        try {
            Sound sound = getSound(path);
            if (sound != null) {
                long id = sound.play(sfxVolume);
                sound.setPitch(id, pitch);
            }
        } catch (Exception e) {
            Gdx.app.error(Constants.TAG, "Error reproduciendo sonido: " + path);
        }
    }
    
    /**
     * Obtiene un sonido del cache o lo carga
     */
    private Sound getSound(String path) {
        if (!soundCache.containsKey(path)) {
            try {
                Sound sound = Gdx.audio.newSound(Gdx.files.internal(path));
                soundCache.put(path, sound);
            } catch (Exception e) {
                Gdx.app.error(Constants.TAG, "Error cargando sonido: " + path);
                return null;
            }
        }
        return soundCache.get(path);
    }
    
    /**
     * Precarga un sonido
     */
    public void preloadSound(String path) {
        getSound(path);
    }
    
    /**
     * Establece el volumen de efectos
     */
    public void setSfxVolume(float volume) {
        this.sfxVolume = Math.max(0f, Math.min(1f, volume));
    }
    
    public float getSfxVolume() {
        return sfxVolume;
    }
    
    public void setSfxEnabled(boolean enabled) {
        this.sfxEnabled = enabled;
    }
    
    // ========== SONIDOS PREDEFINIDOS ==========
    
    public void playCardFlip() {
        playSound(Constants.PATH_SFX + "card_flip.ogg");
    }
    
    public void playMatch() {
        playSound(Constants.PATH_SFX + "match.ogg");
    }
    
    public void playNoMatch() {
        playSound(Constants.PATH_SFX + "no_match.ogg");
    }
    
    public void playVictory() {
        playSound(Constants.PATH_SFX + "victory.ogg");
    }
    
    public void playDefeat() {
        playSound(Constants.PATH_SFX + "defeat.ogg");
    }
    
    public void playButtonClick() {
        playSound(Constants.PATH_SFX + "button_click.ogg");
    }
    
    public void playCoinCollect() {
        playSound(Constants.PATH_SFX + "coin_collect.ogg");
    }
    
    public void playTimerWarning() {
        playSound(Constants.PATH_SFX + "timer_warning.ogg");
    }
    
    @Override
    public void dispose() {
        stopMusic();
        
        for (Sound sound : soundCache.values()) {
            sound.dispose();
        }
        soundCache.clear();
    }
}