package project.kristiyan.commands.admin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;

import java.awt.*;

public class RemoveAdminCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String pure_text = event.getMessage().getContentStripped();
        User author = event.getAuthor();

        if (!event.isFromGuild() || event.isWebhookMessage() ||
                author.isBot() ||
                !pure_text.startsWith("!remove_admin")) {
            return;
        }

        if (!App.adminDao.isAdmin(author.getIdLong())) {
            return;
        }

        PrivateChannel channel = author.openPrivateChannel().complete();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        try {
            long adminToRemoveId = Long.parseLong(
                            pure_text.split(" ", 2)[1]);

            App.adminDao.removeAdmin(adminToRemoveId);

            embedBuilder.setColor(Color.GREEN);
            embedBuilder.setTitle("Removed "+adminToRemoveId+" as an Admin");

        } catch (Exception ignore) {
            embedBuilder.setColor(Color.RED);
            embedBuilder.setTitle("You have given an invalid user ID or something in the database");
        }
        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
