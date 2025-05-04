package project.kristiyan.listeners;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import project.kristiyan.App;
import project.kristiyan.models.DUser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class GuildListener extends ListenerAdapter {

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("subscribe") && event.getFocusedOption().getName().equals("time")) {
            event.replyChoices(new Command.Choice("8:30 Europe/Helsinki", "8:30 Europe/Helsinki")).queue();
        }
    }






    /**
     * Registers slash commands as GUILD commands (max 100).
     * These commands will update instantly and are great for testing.
     */
    @Override
    public void onGuildReady(GuildReadyEvent event) {
        event.getGuild().updateCommands().addCommands(
                Commands.slash("subscribe",
                                "You will subscribe to a daily messages about God's promises.")
                        .addOption(OptionType.STRING,
                                "time",
                                "Your time when you would like to receive the message. Example: 8:30 Europe/Helsinki", true),

                Commands.slash("unsubscribe",
                        "You will unsubscribe."),

                Commands.slash("subscribers",
                        "You will see the subscribers."),

                Commands.slash("music",
                        "Joins vc and plays music.")
                        .addOption(OptionType.STRING,
                                "source",
                                "Supports: .mp3 files and playlists", true),

                Commands.slash("pause", "Pause the current track"),
                Commands.slash("resume", "Resume the current track"),
                Commands.slash("skip", "Skip to the next track in queue"),
                Commands.slash("stop", "Stop playing and clear the queue"),
                Commands.slash("queue", "Display the current playlist queue"),
                Commands.slash("volume", "Set the player volume (0-100)")
                        .addOption(OptionType.INTEGER,
                                "level",
                                "Volume level between 0 and 100", true)
        ).queue();

        App.jda.getPresence().setActivity(Activity.listening("The bot may sent the promises twice. Music bot and daily promises."));

        // run every 60 seconds / 1 min
        new Timer().schedule(new TimerTask(){
            public void run(){

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

                        String timezone = parts[1];
                        ZoneId zoneId = ZoneId.of(timezone);

                        ZonedDateTime currentTimeForUser = ZonedDateTime.now(zoneId);

                        ZonedDateTime expectedTimeForSendingTheMessage = ZonedDateTime.of(
                                LocalDateTime.now()
                                        .withHour(hour)
                                        .withMinute(mins),
                                zoneId
                        );

                        ZonedDateTime maxPeriodForSendingTheMessage = expectedTimeForSendingTheMessage.plusMinutes(5);

                        boolean timesEqualForSending = expectedTimeForSendingTheMessage.equals(currentTimeForUser) || maxPeriodForSendingTheMessage.equals(currentTimeForUser);
                        boolean inBetweenTimes = expectedTimeForSendingTheMessage.isBefore(currentTimeForUser) || maxPeriodForSendingTheMessage.isAfter(currentTimeForUser);

                        if ((timesEqualForSending || inBetweenTimes) && !Duser.has_sent) {
                            // we still are in the time for sending the message.

                            // PrivateChannel = DM
                            PrivateChannel channel = user.openPrivateChannel().useCache(true).complete();

                            channel.sendMessageEmbeds(
                                    App.utils.buildEmbed(
                                            Files.readString(Paths.get(files.getFirst().getPath()))
                                    )
                            ).queue();

                            // then update the database that we have send the message.

                            long id = user.getIdLong();
                            App.database.updateUser(id, true);

                            new Thread(() -> {
                                try {
                                    // 7 mins. 420000
                                    Thread.currentThread().wait(420000);
                                    App.database.updateUser(id, false);
                                } catch (Exception e) {}
                            }).start();
                        }

                    } catch (Exception e) {
                    }
                }

            }},0,60_000);
    }


    /**
     * Registers slash commands as GLOBAL commands (unlimited).
     * These commands may take up to an hour to update.
     */
    @Override
    public void onReady(ReadyEvent event) {
        /*
        event.getJDA().updateCommands().addCommands(
                Commands.slash("subscribe",
                                "You will subscribe to a daily messages about God's promises.")
                        .addOption(OptionType.STRING,
                                "time",
                                "Your time when you would like to receive the message. Example: 8:30 AM EEST"),

                Commands.slash("unsubscribe",
                        "You will unsubscribe."),

                Commands.slash("subscribers",
                        "You will see the subscribers.")
        ).queue();

         */
    }
}
