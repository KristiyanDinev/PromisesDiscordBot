package project.kristiyan.commands.music;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;
import project.kristiyan.audio.GuildMusicManager;

import java.awt.*;

public class ResumeCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("resume")) {
            return;
        }

        Guild guild = event.getGuild();
        GuildMusicManager musicManager = App.utils.getGuildMusicManager(guild);
        if (musicManager == null) {
            return;
        }

        if (musicManager.player.getPlayingTrack() == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("Nothing is currently playing!")
                    .setColor(Color.GREEN)
                    .build()).setEphemeral(true).queue();
            return;
        }

        if (!musicManager.player.isPaused()) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("The player is not paused!")
                    .setColor(Color.GREEN)
                    .build()).setEphemeral(true).queue();
            return;
        }

        musicManager.player.setPaused(false);
        event.replyEmbeds(new EmbedBuilder()
                .setTitle("Resumed the player.")
                .setColor(Color.GREEN)
                .build()).queue();
    }
}
