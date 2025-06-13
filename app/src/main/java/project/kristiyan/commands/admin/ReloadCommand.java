package project.kristiyan.commands.admin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;

import java.awt.*;

public class ReloadCommand extends ListenerAdapter {


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        User author = event.getAuthor();

        if (!event.isFromGuild() || event.isWebhookMessage() ||
                author.isBot() ||
                !event.getMessage().getContentStripped().startsWith("!reload")) {
            return;
        }

        if (!App.adminDao.isAdmin(author.getIdLong())) {
            return;
        }

        PrivateChannel channel = author.openPrivateChannel().complete();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        try {
            App.utility.reloadFiles();

            embedBuilder.setTitle("Successfully Reloaded Files");
            embedBuilder.setColor(Color.GREEN);

        } catch (Exception ignored) {
            embedBuilder.setTitle("Couldn't reload the files.");
            embedBuilder.setColor(Color.RED);
        }

        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
