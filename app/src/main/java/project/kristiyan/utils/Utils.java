package project.kristiyan.utils;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.Nullable;
import project.kristiyan.audio.GuildMusicManager;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    public AudioPlayerManager playerManager;
    public Map<Long, GuildMusicManager> musicManagers;

    public Utils() {
        musicManagers = new HashMap<>();

        playerManager = new DefaultAudioPlayerManager();

        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new LocalAudioSourceManager());

        playerManager.getConfiguration().setFilterHotSwapEnabled(true);
        playerManager.setTrackStuckThreshold(10000);

        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    public void connectToVoiceChannel(AudioManager audioManager, VoiceChannel channel) {
        if (!audioManager.isConnected()) {
            audioManager.openAudioConnection(channel);
        }
    }


    public synchronized GuildMusicManager getGuildMusicManager(@Nullable Guild guild) {
        if (guild == null) {
            return null;
        }
        long guildId = guild.getIdLong();
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }
}
