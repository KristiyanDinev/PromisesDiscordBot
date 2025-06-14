package project.kristiyan.commands.admin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import project.kristiyan.App;
import project.kristiyan.interfaces.ICommand;

import java.awt.*;

public class AddAdminCommand implements ICommand {

    @Override
    public String getName() {
        return "!add_admin";
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
            User newAdmin = App.jda.getUserById(Long.parseLong(args[0]));
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
