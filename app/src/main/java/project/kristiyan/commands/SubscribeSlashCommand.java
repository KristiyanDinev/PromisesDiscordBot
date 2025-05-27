package project.kristiyan.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;
import project.kristiyan.database.entities.UserEntity;

import java.awt.*;

public class SubscribeSlashCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("subscribe")) {
            return;
        }

        Member member = event.getMember();
        if (member == null) {
            return;
        }

        long memberId = member.getIdLong();
        for (UserEntity UserEntity : App.database.getUsers()) {
            if (UserEntity.id == memberId) {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Already subscribed");
                embedBuilder.setColor(Color.GREEN);

                event.replyEmbeds(embedBuilder.build()).queue();
                return;
            }
        }

        OptionMapping optionMapping_time = event.getOption("time");
        if (optionMapping_time == null) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Provide time and timezone. Example: 8:30 Europe/Helsinki");
            embedBuilder.setColor(Color.RED);

            event.replyEmbeds(embedBuilder.build()).queue();
            return;
        }

        String time = optionMapping_time.getAsString();

        String[] parts = time.split(" ");
        if (parts.length != 2 || !parts[0].contains(":")) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Invalid provided time and timezone format");
            embedBuilder.addField("Example" , "8:30 Europe/Helsinki", false);
            embedBuilder.setColor(Color.RED);

            event.replyEmbeds(embedBuilder.build()).queue();
            return;
        }

        try {
            App.database.insertUser(
                    new UserEntity(memberId, member.getEffectiveName(), time, false ,));

        } catch (Exception e) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Error subscribing: "+e);
            embedBuilder.setColor(Color.RED);

            event.replyEmbeds(embedBuilder.build()).queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Subscribed");
        embedBuilder.addField(member.getEffectiveName(), time, false);
        embedBuilder.setColor(Color.GREEN);

        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
