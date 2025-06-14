package project.kristiyan.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.checkerframework.checker.nullness.qual.NonNull;
import project.kristiyan.commands.admin.AdminsCommand;
import project.kristiyan.commands.admin.ReloadCommand;
import project.kristiyan.commands.admin.RemoveAdminCommand;
import project.kristiyan.interfaces.ICommand;
import project.kristiyan.commands.admin.AddAdminCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager extends ListenerAdapter {

    private final List<ICommand> commands = new ArrayList<>();

    public CommandManager() {
        registerCommand(new AddAdminCommand());
        registerCommand(new AdminsCommand());
        registerCommand(new RemoveAdminCommand());
        registerCommand(new ReloadCommand());
    }

    private void registerCommand(ICommand command) {
        commands.add(command);
    }

    @Override
    public void onMessageReceived(@NonNull MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.getAuthor().isBot() || event.isWebhookMessage()) return;

        String[] parts = event.getMessage().getContentStripped().split("\\s+");
        String commandTrigger = parts[0];

        for (ICommand command : commands) {
            if (command.getName().equalsIgnoreCase(commandTrigger)) {
                command.execute(event, Arrays.copyOfRange(parts, 1, parts.length));
                break;
            }
        }
    }
}
