package project.kristiyan.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import project.kristiyan.audio.GuildMusicManager;
import project.kristiyan.models.EmbedModel;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class Utility {
    public AudioPlayerManager playerManager;
    public Map<Long, GuildMusicManager> musicManagers;
    public ObjectMapper objectMapper;

    public Utility() {
        musicManagers = new HashMap<>();
        playerManager = new DefaultAudioPlayerManager();

        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new LocalAudioSourceManager());

        playerManager.getConfiguration().setFilterHotSwapEnabled(true);
        playerManager.setTrackStuckThreshold(10000);

        AudioSourceManagers.registerLocalSource(playerManager);

        objectMapper = new ObjectMapper();
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

    private Color getColor(String colorS) {
        Color color;
        switch (colorS.toLowerCase()) {
            case "black":
                color = Color.BLACK;
                break;
            case "blue":
                color = Color.BLUE;
                break;
            case "cyan":
                color = Color.CYAN;
                break;
            case "darkgray":
                color = Color.DARK_GRAY;
                break;
            case "gray":
                color = Color.GRAY;
                break;
            case "green":
                color = Color.GREEN;
                break;
            case "yellow":
                color = Color.YELLOW;
                break;
            case "lightgray":
                color = Color.LIGHT_GRAY;
                break;
            case "magneta":
                color = Color.MAGENTA;
                break;
            case "orange":
                color = Color.ORANGE;
                break;
            case "pink":
                color = Color.PINK;
                break;
            case "red":
                color = Color.RED;
                break;
            case "white":
                color = Color.WHITE;
                break;
            default:
                color = Color.green;
        }
        return color;
    }

    public List<MessageEmbed> buildEmbed(String content) throws JsonProcessingException {
        EmbedModel model = objectMapper.readValue(content, EmbedModel.class);

        List<MessageEmbed> embeds = new ArrayList<>();

        EmbedBuilder embedBuilder = getEmbedBuilder(model);

        int i = 0;
        for (Map<String, Object> field : model.fields) {
            boolean inline = Boolean.parseBoolean(
                    String.valueOf(field.getOrDefault("inline", false)));

            List<String> name_texts = _getSplitStringForEmbed(String.valueOf(field.get("name")));
            List<String> value_texts = _getSplitStringForEmbed(String.valueOf(field.get("value")));
            
            if (name_texts.size() == 1 && value_texts.size() == 1) {
                embedBuilder.addField(name_texts.getFirst(), value_texts.getFirst(), inline);
                i++;
                if (i % 25 == 0) {
                    embeds.add(embedBuilder.build());
                    embedBuilder = new EmbedBuilder();
                    embedBuilder.setAuthor("Continuing");
                }
                
            } else {
                for (String name_text : name_texts) {
                    embedBuilder.addField(name_text, ".", inline);
                    i++;
                    if (i % 25 == 0) {
                        embeds.add(embedBuilder.build());
                        embedBuilder = new EmbedBuilder();
                        embedBuilder.setAuthor("Continuing");
                    }
                }

                for (String value_text : value_texts) {
                    embedBuilder.addField(".", value_text, inline);
                    i++;
                    if (i % 25 == 0) {
                        embeds.add(embedBuilder.build());
                        embedBuilder = new EmbedBuilder();
                        embedBuilder.setAuthor("Continuing");
                    }
                }
            }
        }

        if (embeds.isEmpty()) {
            embeds.add(embedBuilder.build());
        }

        return embeds;
    }


    private List<String> _getSplitStringForEmbed(String value) {
        List<String> split = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();
        int word_count = 0;
        for (String word : value.split(" ")) {
            word_count++;
            stringBuilder.append(word).append(" ");

            if (word_count % 100 == 0) {
                split.add(stringBuilder.toString());
                stringBuilder = new StringBuilder();
            }
        }

        if (split.isEmpty()) {
            split.add(stringBuilder.toString());
        }

        return split;
    }
    


    @NotNull
    private EmbedBuilder getEmbedBuilder(EmbedModel model) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (!model.color.isEmpty()) {
            embedBuilder.setColor(getColor(model.color));
        }

        if (!model.author.isEmpty()) {
            embedBuilder.setAuthor(model.author);
        }

        if (!model.footer.isEmpty()) {
            embedBuilder.setFooter(model.footer);
        }

        if (!model.url.isEmpty()) {
            embedBuilder.setUrl(model.url);
        }

        if (!model.image.isEmpty()) {
            embedBuilder.setImage(model.image);
        }
        return embedBuilder;
    }
}
