import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaException;
import javafx.util.Duration;

public class MusicPlayer {
    
    private Runnable onSongEnd;
    
    public void setOnSongEnd(Runnable r) {
        this.onSongEnd = r;
    }

    static { 
        JFXPanel fxPanel = new JFXPanel(); 
    }

    private MediaPlayer mediaPlayer;
    private File currentSongFile;
    private File tempWavFile; // For FLAC-to-WAV conversion
    private boolean isPlaying = false;
    private double savedPosition = 0.0;

    public void loadSong(File songFile) {
        stop();
        this.currentSongFile = songFile;
        play();
    }
    
    // Always uses Platform.runLater() to ensure MediaPlayer is created on the JavaFX thread.
    // When called from OnEndOfMedia (FX thread), the old player already ended so stop() is safe.
    // When called from AWT thread (Next/Previous buttons), properly stops old player first.
    public void playNewSong(File songFile) {
        this.currentSongFile = songFile;
        this.savedPosition = 0.0;
        this.isPlaying = false;
        // Stop and dispose the old player
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        // Clean up previous temp WAV file
        if (tempWavFile != null) {
            tempWavFile.delete();
            tempWavFile = null;
        }
        // Always create on the JavaFX thread via Platform.runLater
        Platform.runLater(() -> createMediaPlayer());
    }

    public void play() {
        if(currentSongFile == null) {
            return;
        }
        Platform.runLater(() -> createMediaPlayer());
    }
    
    /**
     * Converts a FLAC file to a temporary WAV file using javax.sound.sampled.
     * Returns the WAV file, or null if conversion fails.
     */
    private File convertFlacToWav(File flacFile) {
        try {
            // Read the FLAC file using the jflac-codec SPI
            AudioInputStream flacStream = AudioSystem.getAudioInputStream(flacFile);
            AudioFormat baseFormat = flacStream.getFormat();
            
            // Convert to PCM signed 16-bit (WAV format)
            AudioFormat pcmFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false
            );
            
            AudioInputStream pcmStream = AudioSystem.getAudioInputStream(pcmFormat, flacStream);
            
            // Write to a temporary WAV file
            File wavFile = File.createTempFile("sakuraplayer_", ".wav");
            wavFile.deleteOnExit();
            AudioSystem.write(pcmStream, AudioFileFormat.Type.WAVE, wavFile);
            
            pcmStream.close();
            flacStream.close();
            
            return wavFile;
        } catch (Exception e) {
            System.err.println("Failed to convert FLAC to WAV: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // This must be called on the JavaFX thread
    private void createMediaPlayer() {
        try {
            File fileToPlay = currentSongFile;
            
            // Check if this is a FLAC file - JavaFX doesn't support FLAC natively
            String name = currentSongFile.getName().toLowerCase();
            if (name.endsWith(".flac")) {
                File wavFile = convertFlacToWav(currentSongFile);
                if (wavFile != null) {
                    fileToPlay = wavFile;
                    tempWavFile = wavFile;
                } else {
                    System.err.println("Could not convert FLAC file: " + currentSongFile.getName());
                    return;
                }
            }
            
            // Fix JavaFX URI bug - use proper URI encoding
            URI fileUri = fileToPlay.toURI();
            String uri = fileUri.toString();
            
            // Better URI encoding - handle all special characters
            uri = uri.replace(" ", "%20");
            uri = uri.replace("#", "%23");
            uri = uri.replace("[", "%5B");
            uri = uri.replace("]", "%5D");
            uri = uri.replace("'", "%27");
            uri = uri.replace("(", "%28");
            uri = uri.replace(")", "%29");
            uri = uri.replace("!", "%21");
            uri = uri.replace("~", "%7E");
            
            Media media = new Media(uri);
            
            mediaPlayer = new MediaPlayer(media);
            
            // Add error handler
            mediaPlayer.setOnError(() -> {
                if (mediaPlayer.getError() != null) {
                    mediaPlayer.getError().printStackTrace();
                }
            });
            
            mediaPlayer.setOnReady(() -> {
                mediaPlayer.seek(Duration.seconds(savedPosition));
                mediaPlayer.play();
                isPlaying = true;
            });
            
            mediaPlayer.setOnEndOfMedia(() -> {
                // Directly call the callback - we're already on the FX thread
                if (onSongEnd != null) {
                    onSongEnd.run();
                }
            });
        } catch (MediaException e) {
            e.printStackTrace(); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }

    public void pause() {
        if(mediaPlayer != null && isPlaying) {
            savedPosition = mediaPlayer.getCurrentTime().toSeconds();
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public void resume() {
        if(mediaPlayer != null) {
            mediaPlayer.play();
            isPlaying = true;
        }
    }

    public void stop() {
        if(mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.UNKNOWN) { 
            mediaPlayer.stop(); 
            mediaPlayer.dispose(); 
        }
        mediaPlayer = null;
        isPlaying = false;
        savedPosition = 0.0;
        // Clean up temp WAV file
        if (tempWavFile != null) {
            tempWavFile.delete();
            tempWavFile = null;
        }
    }

    public boolean isPlaying() { 
        return isPlaying; 
    }
    public File getCurrentSong() { return currentSongFile; }

    public double getDuration() {
        if(mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.UNKNOWN 
                                && mediaPlayer.getStatus() != MediaPlayer.Status.DISPOSED) {
            return mediaPlayer.getTotalDuration().toSeconds();
        }
        return 0.0;
    }

    public double getCurrentTime() {
        if(mediaPlayer != null) {
            return mediaPlayer.getCurrentTime().toSeconds();
        }
        return 0.0;
    }

    public void seek(double seconds) {
        if(mediaPlayer != null) {
            Platform.runLater(() -> {
                mediaPlayer.seek(Duration.seconds(seconds));
            });
        }
    }
}
