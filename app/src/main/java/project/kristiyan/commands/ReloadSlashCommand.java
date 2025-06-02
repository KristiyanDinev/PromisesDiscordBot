package project.kristiyan.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;

import java.awt.*;

public class ReloadSlashCommand extends ListenerAdapter {


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("reload")) {
            return;
        }

        try {
            App.utility.reloadFiles();

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Successfully Reloaded Files");
            embedBuilder.setColor(Color.GREEN);

            event.replyEmbeds(embedBuilder.build()).queue();

        } catch (Exception ignored) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Couldn't reload the files.");
            embedBuilder.setColor(Color.RED);

            event.replyEmbeds(embedBuilder.build()).queue();
        }
    }
}
