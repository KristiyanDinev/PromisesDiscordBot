package project.kristiyan.commands.admin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;
import project.kristiyan.database.entities.AdminEntity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AdminsCommand extends ListenerAdapter {


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String pure_text = event.getMessage().getContentStripped();
        User author = event.getAuthor();

        if (!event.isFromGuild() || event.isWebhookMessage() ||
                author.isBot() ||
                !pure_text.startsWith("!admins")) {
            return;
        }

        if (!App.adminDao.isAdmin(author.getIdLong())) {
            return;
        }

        PrivateChannel channel = author.openPrivateChannel().complete();
        List<AdminEntity> admins = App.adminDao.getAdmins();
        List<MessageEmbed> embeds = new ArrayList<>();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Admins");
        embedBuilder.setColor(Color.GREEN);

        for (int i = 0; i < admins.size(); i++) {
            AdminEntity admin = admins.get(i);
            embedBuilder.addField(admin.name, String.valueOf(admin.user_id), false);

            if ((i+ 1) % 25 == 0) {
                embeds.add(embedBuilder.build());
                embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Continuing");
                embedBuilder.setColor(Color.GREEN);
            }
        }

        if (embeds.isEmpty()) {
            embeds.add(embedBuilder.build());
        }

        channel.sendMessageEmbeds(embeds).queue();
    }
}
