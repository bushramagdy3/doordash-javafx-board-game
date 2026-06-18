package game.gui.utils;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;

/**
 * Centralized audio manager for the DoorDasH game.
 *
 * Sound files are expected in: /game/gui/assets/sounds/
 */
public class SoundManager {

    // Singleton

    private static SoundManager instance;

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    // State
    private boolean muted = false;

    /*
     * Lower background music volume so sound effects are clearer.
     * Increase to 0.35 if it becomes too quiet.
     */
    private static final double BG_VOLUME = 0.25;

    /** Currently playing background music player. Only one at a time. */
    private MediaPlayer currentBgMusic;

    // Sound keys

    public static final String BUTTON_CLICK       = "click.mp3";
    public static final String CARD_POPUP         = "card popup.wav";
    public static final String DICE_ROLL          = "diceroll.mp3";
    public static final String WIN                = "win.wav";
    public static final String GAME_OVER          = "game over.wav";
    public static final String CONTAMINATION_SOCK = "contamination sock.wav";
    public static final String CONVEYOR_BELT      = "conveyor.mp3";
    public static final String DOOR               = "door.mp3";
    public static final String INTRO_BG           = "intro bg music.mp3";
    public static final String GAMEPLAY_BG        = "gameplay bg music.mp3";
    public static final String ERROR_POPUP        = "error.wav";
    public static final String POWERUP            = "powerup.wav";
    public static final String MONSTER_MOVE       = "move cell.wav";

    // Public API

    /** Toggle mute state. Returns the new muted state. */
    public boolean toggleMute() {
        muted = !muted;

        if (currentBgMusic != null) {
            currentBgMusic.setVolume(muted ? 0 : BG_VOLUME);
        }

        return muted;
    }

    /** Set muted state directly. */
    public void setMuted(boolean muted) {
        this.muted = muted;

        if (currentBgMusic != null) {
            currentBgMusic.setVolume(muted ? 0 : BG_VOLUME);
        }
    }

    public boolean isMuted() {
        return muted;
    }

    /**
     * Play a short one-shot sound effect.
     * Silently ignored if the file is missing or muted.
     */
    public void playSound(String fileName) {
        if (muted) {
            return;
        }

        try {
            URL url = getClass().getResource("/game/gui/assets/sounds/" + fileName);

            if (url == null) {
                return;
            }

            AudioClip clip = new AudioClip(url.toExternalForm());

            /*
             * Sound effects stay louder than background music.
             * You can lower this later if effects become too loud.
             */
            clip.setVolume(1.0);
            clip.play();

        } catch (Exception e) {
            // Audio is non-critical, so gameplay should continue.
        }
    }

    /**
     * Start looping background music.
     * Stops any previously playing background track first.
     */
    public void playBgMusic(String fileName) {
        stopBgMusic();

        try {
            URL url = getClass().getResource("/game/gui/assets/sounds/" + fileName);

            if (url == null) {
                return;
            }

            Media media = new Media(url.toExternalForm());
            currentBgMusic = new MediaPlayer(media);

            currentBgMusic.setCycleCount(MediaPlayer.INDEFINITE);
            currentBgMusic.setVolume(muted ? 0 : BG_VOLUME);
            currentBgMusic.play();

        } catch (Exception e) {
            // Audio is non-critical, so gameplay should continue.
        }
    }

    /** Stop background music if any is playing. */
    public void stopBgMusic() {
        if (currentBgMusic != null) {
            try {
                currentBgMusic.stop();
                currentBgMusic.dispose();
            } catch (Exception ignored) {
            }

            currentBgMusic = null;
        }
    }

    /**
     * Fade out the current background music over millis milliseconds,
     * then stop it.
     */
    public void fadeBgMusic(int millis) {
        if (currentBgMusic == null) {
            return;
        }

        MediaPlayer player = currentBgMusic;
        double startVolume = player.getVolume();

        javafx.animation.Timeline fade = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                Duration.ZERO,
                new javafx.animation.KeyValue(player.volumeProperty(), startVolume)
            ),
            new javafx.animation.KeyFrame(
                Duration.millis(millis),
                new javafx.animation.KeyValue(player.volumeProperty(), 0.0)
            )
        );

        fade.setOnFinished(e -> {
            try {
                player.stop();
                player.dispose();
            } catch (Exception ignored) {
            }

            if (currentBgMusic == player) {
                currentBgMusic = null;
            }
        });

        fade.play();
    }
}