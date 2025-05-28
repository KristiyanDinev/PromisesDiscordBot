package project.kristiyan.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;
import project.kristiyan.database.entities.UserEntity;
import project.kristiyan.enums.Services;
import project.kristiyan.utilities.TimerUtility;

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

        OptionMapping timeMapping = event.getOption("time");
        OptionMapping serviceMapping = event.getOption("service");
        if (timeMapping == null || serviceMapping == null) {
            return;
        }

        String time = timeMapping.getAsString();
        if (!TimerUtility.isValidTimeFormat(time)) {
            EmbedBuilder error = new EmbedBuilder();
            error.setTitle("Invalid time format. Try: Hours:Minutes Timezone. Example: 8:30 Europe/Helsinki");
            error.setColor(Color.RED);
            event.replyEmbeds(error.build()).queue();
            return;
        }

        Services services = Services.valueOf(serviceMapping.getAsString());

        EmbedBuilder _alreadySub = new EmbedBuilder();
        _alreadySub.setTitle("Already subscribed the "+services.name()+" service.");
        _alreadySub.setColor(Color.GREEN);

        MessageEmbed alreadySub = _alreadySub.build();

        long user_id = member.getIdLong();
        if (services.equals(Services.Promises)) {
            if (App.promiseDao.checkIfPromiseEntityExistsByUserId(user_id)) {
                event.replyEmbeds(alreadySub).queue();
                return;
            }

            App.promiseDao.subscribe(
                    new UserEntity(user_id, member.getEffectiveName()),
                    time);

        } else if (services.equals(Services.Reminders)) {
            if (App.reminderDao.checkIfReminderEntityExistsByUserId(user_id)) {
                event.replyEmbeds(alreadySub).queue();
                return;
            }

            App.reminderDao.subscribe(
                    new UserEntity(user_id, member.getEffectiveName()),
                    time);
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Subscribed");
        embedBuilder.setAuthor("Service - "+services.name());
        embedBuilder.addField(member.getEffectiveName(), time, false);
        embedBuilder.setColor(Color.GREEN);

        event.replyEmbeds(embedBuilder.build()).queue();
    }
}
