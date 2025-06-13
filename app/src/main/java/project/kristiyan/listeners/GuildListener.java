package project.kristiyan.listeners;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;
import project.kristiyan.enums.Services;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Arrays;

public class GuildListener extends ListenerAdapter {

    /**
     * Registers slash commands as GUILD commands (max 100).
     * These commands will update instantly and are great for testing.
     */
    @Override
    public void onGuildReady(@Nonnull GuildReadyEvent event) {
        OptionData playlistOption = getPlaylistOption();

        OptionData serviceOption = new OptionData(OptionType.STRING,
                "service",
                "Specify a service. Example: Promises",
                true)
                .addChoice(Services.Promises.name(), Services.Promises.name())
                .addChoice(Services.Reminders.name(), Services.Reminders.name());

        OptionData timeOption = new OptionData(OptionType.STRING,
                "time",
                "The time to send you the message. Example: 8:30 Europe/Helsinki",
                true);

        event.getGuild().updateCommands().addCommands(
                Commands.slash("subscribe",
                                "Subscribe for one of our services")
                        .addOptions(timeOption, serviceOption),

                Commands.slash("unsubscribe",
                        "Unsubscribe from one of our services")
                        .addOptions(serviceOption),

                Commands.slash("subscribers",
                        "Show the subscribers")
                        .addOptions(serviceOption)
                        .addOption(OptionType.INTEGER,
                                "page",
                                "Page number",
                                true),

                Commands.slash("music",
                        "Joins vc and plays music")
                        .addOptions(playlistOption),

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

        App.jda.getPresence()
                .setActivity(
                        Activity.listening(
                  "Music and message bot"));
    }

    @NotNull
    private static OptionData getPlaylistOption() {
        File playlists = new File(App.playlists);
        if (!playlists.exists()) {
            playlists.mkdir();
        }

        File[] files = playlists.listFiles();
        if (files == null) {
            files = new File[]{};
        }

        OptionData playlistOption = new OptionData(OptionType.STRING,
                "source",
                "Supports: .mp3 files and playlists",
                true);

        for (File file : files) {
            playlistOption = playlistOption.addChoice(file.getName(), file.getName());
        }
        return playlistOption;
    }
}
