package project.kristiyan.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;
import project.kristiyan.database.entities.PromiseEntity;
import project.kristiyan.database.entities.ReminderEntity;
import project.kristiyan.enums.Services;

import java.awt.*;

public class SubscribersSlashCommand extends ListenerAdapter {


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("subscribers")) {
            return;
        }

        OptionMapping pageMapping = event.getOption("page");
        OptionMapping serviceMapping = event.getOption("service");
        if (pageMapping == null || serviceMapping == null) {
            return;
        }

        int page = pageMapping.getAsInt();
        Services services = Services.valueOf(serviceMapping.getAsString());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Subscribers");
        embedBuilder.setColor(Color.GREEN);

        if (services.equals(Services.Promises)) {
            embedBuilder.setAuthor("Promises Service - Page "+page);

            for (PromiseEntity promiseEntity : App.promiseDao.getUsers(page)) {
                embedBuilder.addField(promiseEntity.userEntity.name,
                        promiseEntity.time, false);
            }

        } else if (services.equals(Services.Reminders)) {
            embedBuilder.setAuthor("Reminders Service - Page "+page);

            for (ReminderEntity reminderEntity : App.reminderDao.getUsers(page)) {
                embedBuilder.addField(reminderEntity.userEntity.name,
                        reminderEntity.time, false);
            }
        }

        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
