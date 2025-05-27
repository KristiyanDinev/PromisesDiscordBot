package project.kristiyan.commands.music;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;
import project.kristiyan.audio.GuildMusicManager;

import java.awt.*;

public class VolumeCommand extends ListenerAdapter {


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("volume")) {
            return;
        }

        Guild guild = event.getGuild();
        GuildMusicManager musicManager = App.utility.getGuildMusicManager(guild);
        if (musicManager == null) {
            return;
        }

        // Check if something is playing
        if (musicManager.player.getPlayingTrack() == null) {
            event.replyEmbeds(
                    new EmbedBuilder()
                            .setTitle("Nothing is currently playing!")
                            .setColor(Color.GREEN)
                            .build()).setEphemeral(true).queue();
            return;
        }

        // Get the volume level from command options
        OptionMapping optionMapping = event.getOption("level");
        if (optionMapping == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("Level for volume is required.")
                    .setColor(Color.RED)
                    .build()).setEphemeral(true).queue();
            return;
        }

        int volumeLevel = optionMapping.getAsInt();

        // Validate volume range
        if (volumeLevel < 0 || volumeLevel > 100) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("Volume must be between 0 and 100.")
                    .setColor(Color.RED)
                    .build()).setEphemeral(true).queue();
            return;
        }

        // Get current volume for comparison
        int previousVolume = musicManager.player.getVolume();

        // Set the volume
        musicManager.player.setVolume(volumeLevel);

        // Create a descriptive message based on change
        String volumeDescription;
        String emoji;

        if (volumeLevel == 0) {
            volumeDescription = "muted";
            emoji = "🔇";

        } else if (volumeLevel < 30) {
            volumeDescription = "low";
            emoji = "🔈";

        } else if (volumeLevel < 70) {
            volumeDescription = "medium";
            emoji = "🔉";

        } else {
            volumeDescription = "high";
            emoji = "🔊";
        }

        // Indicate whether volume is increased, decreased, or unchanged
        String changeDescription;
        if (volumeLevel > previousVolume) {
            changeDescription = "increased";

        } else if (volumeLevel < previousVolume) {
            changeDescription = "decreased";

        } else {
            changeDescription = "set";
        }

        event.replyEmbeds(new EmbedBuilder()
                .addField(emoji,
                        " Volume " + changeDescription + " to " + volumeLevel + " (" + volumeDescription + ")",
                        false)
                .setColor(Color.GREEN)
                .build()).queue();
    }
}
