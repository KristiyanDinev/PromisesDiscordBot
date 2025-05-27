package project.kristiyan.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import project.kristiyan.App;
import project.kristiyan.audio.GuildMusicManager;
import project.kristiyan.database.entities.UserEntity;
import project.kristiyan.models.EmbedModel;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.List;

public class Utility {
    public AudioPlayerManager playerManager;
    public Map<Long, GuildMusicManager> musicManagers;
    public ObjectMapper objectMapper;

    public Utility() {
        musicManagers = new HashMap<>();
        playerManager = new DefaultAudioPlayerManager();

        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new LocalAudioSourceManager());

        playerManager.getConfiguration().setFilterHotSwapEnabled(true);
        playerManager.setTrackStuckThreshold(10000);

        AudioSourceManagers.registerRemoteSources(playerManager);

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

    public MessageEmbed buildEmbed(String content) throws JsonProcessingException {
        EmbedModel model = objectMapper.readValue(content, EmbedModel.class);

        EmbedBuilder embedBuilder = getEmbedBuilder(model);

        for (Map<String, Object> field : model.fields) {
            embedBuilder.addField(
                    String.valueOf(field.get("name")),
                    String.valueOf(field.get("value")),
                    Boolean.parseBoolean(
                            String.valueOf(field.getOrDefault("inline", false))));
        }

        return embedBuilder.build();
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


    // run every 60 seconds / 1 min
    public void runTimer() {
        new Timer().schedule(new TimerTask() {
            public void run() {
                List<File> files = App.utility.getFiles("promises/");
                if (files.isEmpty()) {
                    return;
                }

                Collections.shuffle(files);

                for (UserEntity duser : App.database.getUsers()) {


                    // user.time = 8:30 Europe/Helsinki

                    try {
                        User user = App.jda.getUserById(duser.id);
                        if (user == null) {
                            continue;
                        }

                        String[] parts = duser.time.split(" ");

                        String[] timeParts = parts[0].split(":");
                        int hour = Integer.parseInt(timeParts[0]);
                        int mins = Integer.parseInt(timeParts[1]);

                        ZonedDateTime currentTimeForUser = ZonedDateTime.now(ZoneId.of(parts[1]));

                        // Current time components
                        int currentMinute = currentTimeForUser.getMinute();

                        // shouldSendNow: Check if we're in the 5-minute window for sending
                        if ((currentTimeForUser.getHour() == hour &&
                                currentMinute >= mins &&
                                currentMinute <= (mins + 5)) && !duser.has_sent) {

                            long id = user.getIdLong();

                            // we still are in the time for sending the message.

                            // PrivateChannel = DM
                            PrivateChannel channel = user.openPrivateChannel().useCache(true).complete();

                            channel.sendMessageEmbeds(
                                    App.utility.buildEmbed(
                                            Files.readString(Paths.get(files.getFirst().getPath()))
                                    )
                            ).queue(success -> {

                                        try {
                                            App.database.updateUser(id, true);
                                        } catch (Exception e) {
                                        }
                                        scheduleResetHasSent(id);

                                    },
                                    error -> {
                                        try {
                                            App.database.updateUser(id, false);
                                        } catch (Exception ex) {
                                        }
                                    });
                        }

                    } catch (Exception e) {
                        try {
                            App.database.updateUser(duser.id, false);
                        } catch (Exception ex) {
                        }
                        System.out.println(e.getMessage());
                    }
                }

            }
        }, 0, 60_000);
    }

    private static void scheduleResetHasSent(long userId) {
        new Thread(() -> {
            try {
                // Use Thread.sleep, not wait
                Thread.sleep(420000); // 7 minutes
                App.database.updateUser(userId, false);
            } catch (Exception e) {
            }
        }).start();
    }
}
