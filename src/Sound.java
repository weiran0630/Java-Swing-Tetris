import javax.sound.sampled.*;
import java.io.BufferedInputStream;

public class Sound {
    private String uriToSound;
    private Clip clip;

    public Sound(String url) {
        this.uriToSound = url;
        this.init();
    }

    private void init() {
        // Try to find the resource (music file) with a given name
        try (var in = getClass().getResourceAsStream(this.uriToSound)) {
            var bufferedIS = new BufferedInputStream(in);

            // Obtains an audio input stream from the provided input stream.
            try (var audioIS = AudioSystem.getAudioInputStream(bufferedIS)) {
                // Obtains a clip that can be used for playing back an audio file or an audio stream.
                this.clip = AudioSystem.getClip();
                this.clip.open(audioIS); // Become operational.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void play() {
        if (this.clip != null) {
            this.clip.start();
        }
    }

    public void pause() {
        if (this.clip != null) {
            this.clip.stop();
        }
    }

    public void stop() {
        if (this.clip != null) {
            this.clip.close();
        }
    }
}
