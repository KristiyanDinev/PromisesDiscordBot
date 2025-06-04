package project.kristiyan.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

public class AudioPlayerSendHandler implements AudioSendHandler {
    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);

    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    @Override
    public boolean canProvide() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        byte[] data = lastFrame.getData();
        buffer.clear();        // Reset for new data
        buffer.put(data);      // Fill with new audio
        buffer.flip();         // Prepare for read
        return buffer;
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
