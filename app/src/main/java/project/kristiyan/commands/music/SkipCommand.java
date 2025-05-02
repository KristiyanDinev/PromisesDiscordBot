package project.kristiyan.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;
import project.kristiyan.audio.GuildMusicManager;

import java.awt.*;

public class SkipCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("skip")) {
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

        String currentTrackTitle = musicManager.player.getPlayingTrack().getInfo().title;
        musicManager.scheduler.nextTrack();

        AudioTrack nextTrack = musicManager.player.getPlayingTrack();
        if (nextTrack != null) {
            event.replyEmbeds(new EmbedBuilder()
                    .addField("Skipped: `" + currentTrackTitle + "`",
                            ". Now playing: `" + nextTrack.getInfo().title + "`", false)
                    .setColor(Color.GREEN)
                    .build()).queue();

        } else {
            event.replyEmbeds(new EmbedBuilder()
                    .addField("Skipped: `" + currentTrackTitle + "`",
                            ". No more tracks in queue.", false)
                    .setColor(Color.GREEN)
                    .build()).queue();
        }
    }
}
