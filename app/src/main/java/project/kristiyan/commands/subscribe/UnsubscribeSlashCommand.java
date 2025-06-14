package project.kristiyan.commands.subscribe;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;
import project.kristiyan.enums.Services;

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

        OptionMapping serviceMapping = event.getOption("service");
        if (serviceMapping == null) {
            return;
        }

        Services services = Services.valueOf(serviceMapping.getAsString());

        long user_id = member.getIdLong();

        EmbedBuilder _unsubscribedEmbed = new EmbedBuilder();
        _unsubscribedEmbed.setTitle("Unsubscribed from "+services.name()+" service");
        _unsubscribedEmbed.setColor(Color.GREEN);

        EmbedBuilder _errorUnsubscribedEmbed = new EmbedBuilder();
        _errorUnsubscribedEmbed.setTitle("Couldn't unsubscribe to "+services.name()+" service");
        _errorUnsubscribedEmbed.setColor(Color.RED);

        MessageEmbed ErrorUnsubscribedEmbed = _errorUnsubscribedEmbed.build();

        if (services.equals(Services.Promises) && !App.promiseDao.unsubscribe(user_id)) {
            event.replyEmbeds(ErrorUnsubscribedEmbed).queue();
            return;

        } else if (services.equals(Services.Reminders) && !App.reminderDao.unsubscribe(user_id)) {
            event.replyEmbeds(ErrorUnsubscribedEmbed).queue();
            return;
        }

        event.replyEmbeds(_unsubscribedEmbed.build()).queue();
    }
}
