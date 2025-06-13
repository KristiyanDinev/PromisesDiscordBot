package project.kristiyan.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import org.jetbrains.annotations.NotNull;
import project.kristiyan.App;

import java.util.List;

public class ButtonListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Message message = event.getMessage();
        if (!message.getAuthor().equals(App.jda.getSelfUser())) {
            return;
        }

        String id = event.getButton().getId();
        if (id == null) {
            return;
        }

        String[] parts = id.split(",");
        if (parts.length != 2) {
            return;
        }

        int page;
        try {
            page = Integer.parseInt(parts[1]);

            if (page < 0) {
                throw new Exception();
            }
        } catch (Exception ignored) {
            return;
        }

        String promise = parts[0];

        String content = promise.equals(App.utility.reminderFile) ?
                App.utility.reminderContext :
                App.utility.promises.get(promise);

        List<MessageEmbed> embeds = App.embedUtility.buildEmbedsWithPagination(content);
        if (page >= embeds.size()) {
            return;
        }

        MessageEditAction editAction = message.editMessageEmbeds(embeds.get(page));
        if (page == embeds.size() - 1) {
            // last page
            editAction.setActionRow(App.utility.getBackwardButton(promise, page - 1));

        } else if (page == 0) {
            // at start
            editAction.setActionRow(App.utility.getForwardButton(promise, page + 1));

        } else {
            // at the middle
            editAction.setActionRow(App.utility.getBackwardButton(promise, page - 1),
                    App.utility.getForwardButton(promise, page + 1));
        }

        editAction.queue();

        event.deferReply(true).queue();
    }
}
