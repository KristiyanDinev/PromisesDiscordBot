package project.kristiyan.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;
import project.kristiyan.audio.GuildMusicManager;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MusicCommand extends ListenerAdapter {


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("music")) {
            return;
        }

        Member member = event.getMember();
        if (member == null) {
            return;
        }

        OptionMapping optionMapping = event.getOption("source");
        if (optionMapping == null) {
            event.replyEmbeds(
                    new EmbedBuilder()
                            .setTitle("You need to give the source for the music or stream.")
                            .setColor(Color.RED)
                            .build()).queue();
            return;
        }

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null || !voiceState.inAudioChannel() || voiceState.getChannel() == null) {
            event.replyEmbeds(
                    new EmbedBuilder()
                            .setTitle("Join voice channel, so the bot can join you.")
                            .setColor(Color.RED)
                            .build()).queue();
            return;

        }

        VoiceChannel voiceChannel = voiceState.getChannel().asVoiceChannel();

        String source = optionMapping.getAsString();
        File playlist = new File("playlists/"+source);

        // load and play for the multiple file tracks.
        if (playlist.isDirectory()) {
            File[] files = playlist.listFiles(File::isFile);
            if (files == null || files.length == 0) {
                event.replyEmbeds(
                        new EmbedBuilder()
                                .setTitle("No such source.")
                                .setColor(Color.RED)
                                .build()).queue();
                return;
            }

            List<String> fileNames = new ArrayList<>();
            for (File file : files) {
                fileNames.add(file.getName());
            }

            Collections.shuffle(fileNames);
            musicStarts(event, fileNames);

            for (String name : fileNames) {
                loadAndPlay(event, voiceChannel, "playlists/"+source+"/"+name);
            }

        } else {
            loadAndPlay(event, voiceChannel, source);
            musicStarts(event, List.of(source));
        }
    }

    private void musicStarts(SlashCommandInteractionEvent event, List<String> sources) {
        if (sources.isEmpty()) {
            return;
        }

        String title = "Music Starts | Shuffled";
        int size = sources.size();
        if (size == 1) {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(title)
                    .addField("Playing:", sources.getFirst(), false)
                    .setColor(Color.GREEN)
                    .build()).queue();

        } else {
            event.replyEmbeds(new EmbedBuilder()
                    .setTitle(title)
                    .addField("Loaded songs:", String.valueOf(size), false)
                    .setColor(Color.GREEN)
                    .build()).queue();
        }
    }

    private void loadAndPlay(SlashCommandInteractionEvent event, VoiceChannel channel, String trackUrl) {
        Guild guild = event.getGuild();
        GuildMusicManager musicManager = App.utils.getGuildMusicManager(guild);
        if (musicManager == null) {
            return;
        }
        assert guild != null;

        InteractionHook hook = event.getHook();

        musicManager.player.setVolume(25);

        App.utils.playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                App.utils.connectToVoiceChannel(guild.getAudioManager(), channel);
                musicManager.scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().getFirst();
                }

                App.utils.connectToVoiceChannel(guild.getAudioManager(), channel);
                musicManager.scheduler.queue(firstTrack);
            }

            @Override
            public void noMatches() {
                hook.sendMessageEmbeds(
                        new EmbedBuilder()
                                .addField("Nothing found for: ", trackUrl, false)
                                .setColor(Color.GREEN)
                                .build()).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                hook.sendMessageEmbeds(
                        new EmbedBuilder()
                                .addField("Could not play: ", exception.getMessage(), false)
                                .setColor(Color.GREEN)
                                .build()).queue();
            }
        });
    }
}
