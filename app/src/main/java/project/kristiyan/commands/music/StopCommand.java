package project.kristiyan.commands.music;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;
import project.kristiyan.audio.GuildMusicManager;

import java.awt.*;

public class StopCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("stop")) {
            return;
        }

        Guild guild = event.getGuild();
        if (guild == null) {
            return;
        }

        GuildMusicManager musicManager = App.utils.getGuildMusicManager(guild);
        if (musicManager == null) {
            return;
        }

        musicManager.scheduler.clear();
        musicManager.player.stopTrack();
        musicManager.player.setPaused(false);

        // Disconnect from voice channel
        guild.getAudioManager().closeAudioConnection();

        event.replyEmbeds(new EmbedBuilder()
                .setTitle("Stopped the player and cleared the queue.")
                .setColor(Color.GREEN)
                .build()).queue();
    }
}
