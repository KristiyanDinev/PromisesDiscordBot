package project.kristiyan.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.Nullable;
import project.kristiyan.audio.GuildMusicManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class Utility {
    public AudioPlayerManager playerManager;
    public Map<Long, GuildMusicManager> musicManagers = new HashMap<>();
    public static ObjectMapper objectMapper = new ObjectMapper();

    // name of the file, its context
    public Map<String, String> promises = new HashMap<>();
    public String reminderContext;
    public String reminderFile = "reminder.json";

    public Utility() throws IOException {
        playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new LocalAudioSourceManager());
        playerManager.getConfiguration().setFilterHotSwapEnabled(true);
        playerManager.setTrackStuckThreshold(10000);
        AudioSourceManagers.registerLocalSource(playerManager);
        reloadFiles();
    }

    public void reloadFiles() throws IOException {
        promises.clear();
        reminderContext = Files.readString(Paths.get("reminder.json"));

        for (File file : getFiles("promises/")) {
            promises.put(file.getPath(), Files.readString(Paths.get(file.getPath())));
        }
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

    public List<File> getFiles(String folder) {
        File file = new File(folder);
        if (!file.isDirectory()) {
            return new ArrayList<>();
        }

        return Arrays.stream(Objects.requireNonNull(file.listFiles(File::isFile))).toList();
    }

    public Button getForwardButton(String promiseFile, int page) {
        //  privateChannel.sendMessage("").setActionRow(button).queue();
        return Button.primary(promiseFile+","+page, "⏩");
    }

    public Button getBackwardButton(String promiseFile, int page) {
        //  privateChannel.sendMessage("").setActionRow(button).queue();
        return Button.primary(promiseFile+","+page, "⏪");
    }
}
