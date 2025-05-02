package project.kristiyan.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;
import project.kristiyan.audio.GuildMusicManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class QueueCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("queue")) {
            return;
        }

        Guild guild = event.getGuild();
        GuildMusicManager musicManager = App.utils.getGuildMusicManager(guild);
        if (musicManager == null) {
            return;
        }

        BlockingQueue<AudioTrack> queue = musicManager.scheduler.getQueue();

        if (queue.isEmpty() && musicManager.player.getPlayingTrack() == null) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle("The queue is empty and nothing is playing!")
                    .setColor(Color.GREEN)
                    .build()).queue();


            return;
        }

        List<AudioTrack> trackList = new ArrayList<>(queue);
        AudioTrack currentTrack = musicManager.player.getPlayingTrack();

        EmbedBuilder embedBuilder = new EmbedBuilder();

        if (currentTrack != null) {
            long position = currentTrack.getPosition();
            long duration = currentTrack.getDuration();

            // Create a progress bar
            StringBuilder progressBar = new StringBuilder("[");
            int progressBarLength = 20;
            int progressPosition = (int) ((double) position / duration * progressBarLength);

            for (int i = 0; i < progressBarLength; i++) {
                if (i == progressPosition) {
                    progressBar.append("●"); // Current position marker
                } else {
                    progressBar.append("─");
                }
            }
            progressBar.append("]");

            // Format time as mm:ss
            String currentTime = String.format("%02d:%02d", position / 60000, (position / 1000) % 60);
            String totalTime = String.format("%02d:%02d", duration / 60000, (duration / 1000) % 60);

            AudioTrackInfo info = currentTrack.getInfo();

            embedBuilder.addField("Now playing: ", info.title, false);
            embedBuilder.addField("By: ", info.author, false);
            embedBuilder.addField("Source: ", info.uri, false);
            embedBuilder.addField(currentTime +
                    "/" + totalTime, progressBar.toString(), false);
        }

        int size = trackList.size();
        embedBuilder.addField("Playlist songs: ", String.valueOf(size), false);

        for (int i = 0; i < Math.min(10, size); i++) {
            AudioTrackInfo info = trackList.get(i).getInfo();
            embedBuilder.addField(i + 1 +".", info.title + " by "+info.author, false);
        }

        if (size > 10) {
            embedBuilder.addField("Left songs: ", String.valueOf(size - 10), false);
        }

        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
