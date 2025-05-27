package project.kristiyan.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;
import project.kristiyan.database.entities.UserEntity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SubscribersSlashCommand extends ListenerAdapter {


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("subscribers")) {
            return;
        }

        List<MessageEmbed> embeds = new ArrayList<>();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Subscribers");
        embedBuilder.setAuthor("These users will receive the daily messages");
        embedBuilder.setColor(Color.GREEN);

        int num = 0;

        List<UserEntity> userEntities = App.database.getUsers();
        int size = userEntities.size();
        for (int i = 0; i < size; i++) {
            UserEntity user = userEntities.get(i);
            embedBuilder.addField(user.username, user.time, false);

            if (num >= 25 || (i == size - 1)) {
                embeds.add(embedBuilder.build());

                embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Subscribers");
                embedBuilder.setAuthor("These users will receive the daily messages");
                embedBuilder.setColor(Color.GREEN);
                num = 0;
            }
            num++;
        }

        if (embeds.isEmpty()) {
            EmbedBuilder embedBuilderNoSub = new EmbedBuilder();
            embedBuilderNoSub.setTitle("No subscribers");
            embedBuilderNoSub.setColor(Color.GREEN);

            event.replyEmbeds(embedBuilderNoSub.build()).queue();
            return;
        }

        event.replyEmbeds(embeds).queue();
    }
}
