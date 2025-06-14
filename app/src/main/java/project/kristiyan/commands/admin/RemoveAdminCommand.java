package project.kristiyan.commands.admin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import project.kristiyan.App;
import project.kristiyan.interfaces.ICommand;

import java.awt.*;

public class RemoveAdminCommand implements ICommand {

    @Override
    public String getName() {
        return "!remove_admin";
    }

    @Override
    public void execute(MessageReceivedEvent event, String[] args) {
        User author = event.getAuthor();
        if (!App.adminDao.isAdmin(author.getIdLong())) {
            return;
        }

        PrivateChannel channel = author.openPrivateChannel().complete();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        try {
            long adminToRemoveId = Long.parseLong(args[0]);

            if (!App.adminDao.removeAdmin(adminToRemoveId)) {
                throw new Exception();
            }

            embedBuilder.setColor(Color.GREEN);
            embedBuilder.setTitle("Removed "+adminToRemoveId+" as an Admin");

        } catch (Exception ignore) {
            embedBuilder.setColor(Color.RED);
            embedBuilder.setTitle("You have given an invalid user ID or something in the database");
        }
        channel.sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
