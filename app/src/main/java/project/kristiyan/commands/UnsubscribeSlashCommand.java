package project.kristiyan.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;

import java.awt.*;

public class UnsubscribeSlashCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("unsubscribe")) {
            return;
        }

        Member member = event.getMember();
        if (member == null) {
            return;
        }

        try {
            App.database.deleteUser(member.getIdLong());

            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Unsubscribed");
            embedBuilder.setColor(Color.GREEN);

            event.replyEmbeds(embedBuilder.build()).queue();

        } catch (Exception e) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Error unsubscribing");
            embedBuilder.setColor(Color.RED);

            event.replyEmbeds(embedBuilder.build()).queue();
        }
    }
}
