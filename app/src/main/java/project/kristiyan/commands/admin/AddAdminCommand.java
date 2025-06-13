package project.kristiyan.commands.admin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;

import java.awt.*;

public class AddAdminCommand extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String pure_text = event.getMessage().getContentStripped();
        User author = event.getAuthor();

        if (!event.isFromGuild() || event.isWebhookMessage() ||
                author.isBot() ||
                !pure_text.startsWith("!add_admin")) {
            return;
        }

        if (!App.adminDao.isAdmin(author.getIdLong())) {
            return;
        }

        PrivateChannel channel = author.openPrivateChannel().complete();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        try {
            User newAdmin = App.jda.getUserById(
                    Long.parseLong(
                            pure_text.split(" ", 2)[1]));
            if (newAdmin == null) {
                throw new Exception();
            }

            embedBuilder.setColor(Color.GREEN);
            String name = newAdmin.getEffectiveName();
            if (!App.adminDao.addAdmin(name, newAdmin.getIdLong())) {
                embedBuilder.setTitle("Something went wrong while adding "+name+" as Admin");

            } else {
                embedBuilder.setTitle("Added "+name+" as an Admin");
            }

        } catch (Exception ignore) {
            embedBuilder.setColor(Color.RED);
            embedBuilder.setTitle("Couldn't add this user to Admins. Either this user has nothing in common with the bot (servers) or you have given the wrong user ID.");
        }
        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
