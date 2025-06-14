package project.kristiyan.commands.admin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import project.kristiyan.App;
import project.kristiyan.interfaces.ICommand;

import java.awt.*;

public class ReloadCommand implements ICommand {

    @Override
    public String getName() {
        return "!reload";
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
