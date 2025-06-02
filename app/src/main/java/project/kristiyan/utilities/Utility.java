package project.kristiyan.utilities;

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
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.Nullable;
import project.kristiyan.audio.GuildMusicManager;
import project.kristiyan.models.EmbedModel;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class Utility {
    public AudioPlayerManager playerManager;
    public Map<Long, GuildMusicManager> musicManagers;
    public ObjectMapper objectMapper;

    // name of the file, its context
    public Map<String, String> promises;
    public String reminderContext;
    public String reminderFile = "reminder.json";

    public Utility() throws IOException {
        musicManagers = new HashMap<>();
        playerManager = new DefaultAudioPlayerManager();

        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new LocalAudioSourceManager());

        playerManager.getConfiguration().setFilterHotSwapEnabled(true);
        playerManager.setTrackStuckThreshold(10000);

        AudioSourceManagers.registerLocalSource(playerManager);

        objectMapper = new ObjectMapper();
        promises = new HashMap<>();

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

    public List<MessageEmbed> buildEmbedsWithPagination(String content) {
        EmbedModel model;
        try {
            model = objectMapper.readValue(content, EmbedModel.class);

        } catch (Exception ignored) {
            return new ArrayList<>();
        }

        List<MessageEmbed> embeds = new ArrayList<>();

        List<MessageEmbed.Field> processedFields = new ArrayList<>();

        for (Map<String, Object> field : model.fields) {
            boolean inline = Boolean.parseBoolean(String.valueOf(field.getOrDefault("inline", false)));

            // Split name and value if needed
            List<String> nameChunks = _splitIntoChunks(String.valueOf(field.getOrDefault("name", "")));
            List<String> valueChunks = _splitIntoChunks(String.valueOf(field.getOrDefault("value", "")));

            // Pair them as needed
            if (nameChunks.size() == 1 && valueChunks.size() == 1) {
                processedFields.add(new MessageEmbed.Field(
                        nameChunks.getFirst(),
                        valueChunks.getFirst(),
                        inline
                ));

            } else if (nameChunks.size() == 1) {
                // values are more, then names
                processedFields.add(new MessageEmbed.Field(
                        nameChunks.getFirst(),
                        valueChunks.getFirst(),
                        inline
                ));

                valueChunks.removeFirst();

                for (String valuePart : valueChunks) {
                    processedFields.add(new MessageEmbed.Field(
                            ".",
                            valuePart,
                            inline
                    ));
                }
            } else if (valueChunks.size() == 1) {
                // for sure names are more, then values
                String last = nameChunks.getLast();
                nameChunks.removeLast();

                for (String namePart : nameChunks) {
                    processedFields.add(new MessageEmbed.Field(
                            namePart,
                            ".",
                            inline
                    ));
                }

                processedFields.add(new MessageEmbed.Field(
                        last,
                        valueChunks.getFirst(),
                        inline
                ));


            } else {
                // both are super long
                String last = nameChunks.getLast();
                nameChunks.removeLast();

                for (String namePart : nameChunks) {
                    processedFields.add(new MessageEmbed.Field(
                            namePart,
                            ".",
                            inline
                    ));
                }

                processedFields.add(new MessageEmbed.Field(
                        last,
                        valueChunks.getFirst(),
                        inline
                ));

                valueChunks.removeFirst();

                for (String valuePart : valueChunks) {
                    processedFields.add(new MessageEmbed.Field(
                            ".",
                            valuePart,
                            inline
                    ));
                }
            }
        }

        // Create embeds with 5 fields per page
        int totalFields = processedFields.size();
        int totalPages = (int) Math.ceil(totalFields / 5.0);

        for (int page = 0; page < totalPages; page++) {
            EmbedBuilder embedBuilder = getEmbedBuilder(model);
            embedBuilder.setFooter(model.footer + " Page " + (page + 1) + " of " + totalPages);

            int startIdx = page * 5;
            int endIdx = Math.min(startIdx + 5, totalFields);

            for (int i = startIdx; i < endIdx; i++) {
                embedBuilder.addField(processedFields.get(i));
            }

            embeds.add(embedBuilder.build());
        }

        return embeds;
    }

    private List<String> _splitIntoChunks(String value) {
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

    public Button getForwardButton(String promiseFile, int page) {
        //  privateChannel.sendMessage("").setActionRow(button).queue();
        return Button.primary(promiseFile+","+page, "⏩");
    }

    public Button getBackwardButton(String promiseFile, int page) {
        //  privateChannel.sendMessage("").setActionRow(button).queue();
        return Button.primary(promiseFile+","+page, "⏪");
    }
}
