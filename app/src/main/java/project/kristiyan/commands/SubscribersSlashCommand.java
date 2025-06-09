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

        int page;
        try {
            page = pageMapping.getAsInt();
            if (page < 1) {
                throw new Exception();
            }
        } catch (Exception ignored) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Invalid page. Please specify a positive number bigger than 0");
            embedBuilder.setColor(Color.RED);
            event.replyEmbeds(embedBuilder.build()).queue();
            return;
        }
        Services services = Services.valueOf(serviceMapping.getAsString());

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Subscribers");
        embedBuilder.setColor(Color.GREEN);
        embedBuilder.setAuthor(services.name()+" Service - Page "+page);

        if (services.equals(Services.Promises)) {
            for (PromiseEntity promiseEntity : App.promiseDao.getUsers(page)) {
                embedBuilder.addField(promiseEntity.userEntity.name,
                        promiseEntity.time, false);
            }

        } else if (services.equals(Services.Reminders)) {
            for (ReminderEntity reminderEntity : App.reminderDao.getUsers(page)) {
                embedBuilder.addField(reminderEntity.userEntity.name,
                        reminderEntity.time, false);
            }
        }

        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
