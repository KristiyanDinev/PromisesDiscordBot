package project.kristiyan;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import project.kristiyan.commands.*;
import project.kristiyan.commands.music.*;
import project.kristiyan.database.Database;
import project.kristiyan.listeners.GuildListener;
import project.kristiyan.models.DUser;
import project.kristiyan.utils.Utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class App {
    public static JDA jda;
    public static Database database;
    public static Utils utils;


    public static void main(String[] args) throws Exception {
        database = new Database();
        utils = new Utils();

        JDABuilder builder = JDABuilder.createDefault(
                System.getenv("DISCORD_BOT_TOKEN"),
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES);

        jda = builder.build();

        jda.addEventListener(new GuildListener(),
                new UnsubscribeSlashCommand(),
                new SubscribersSlashCommand(),
                new SubscribeSlashCommand(),
                new MusicCommand(),
                new PauseCommand(),
                new ResumeCommand(),
                new SkipCommand(),
                new StopCommand(),
                new QueueCommand(),
                new VolumeCommand()
        );

        jda.awaitReady();


        // run every 60 seconds / 1 min
        new Timer().schedule(new TimerTask() {
            public void run() {

                List<File> files = App.utils.getFiles("promises/");
                if (files.isEmpty()) {
                    return;
                }

                Collections.shuffle(files);

                for (DUser Duser : App.database.getUsers()) {

                    // user.time = 8:30 Europe/Helsinki

                    try {
                        User user = App.jda.getUserById(Duser.id);
                        if (user == null) {
                            continue;
                        }

                        String[] parts = Duser.time.split(" ");

                        String[] timeParts = parts[0].split(":");
                        int hour = Integer.parseInt(timeParts[0]);
                        int mins = Integer.parseInt(timeParts[1]);

                        ZonedDateTime currentTimeForUser = ZonedDateTime.now(ZoneId.of(parts[1]));

                        // Current time components
                        int currentMinute = currentTimeForUser.getMinute();

                        // shouldSendNow: Check if we're in the 5-minute window for sending
                        if ((currentTimeForUser.getHour() == hour &&
                                currentMinute >= mins &&
                                currentMinute <= (mins + 5)) && !Duser.has_sent) {

                            long id = user.getIdLong();

                            // we still are in the time for sending the message.

                            // PrivateChannel = DM
                            PrivateChannel channel = user.openPrivateChannel().useCache(true).complete();

                            channel.sendMessageEmbeds(
                                    App.utils.buildEmbed(
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
                            App.database.updateUser(Duser.id, false);
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
