package project.kristiyan.listeners;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class GuildListener extends ListenerAdapter {

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals("subscribe") && event.getFocusedOption().getName().equals("time")) {
            event.replyChoices(new Command.Choice("8:30 AM EEST", "8:30 AM EEST")).queue();
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
                                "Your time when you would like to receive the message. Example: 8:30 AM EEST", true),

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
