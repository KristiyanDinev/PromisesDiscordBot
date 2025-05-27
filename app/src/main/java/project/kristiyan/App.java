package project.kristiyan;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import project.kristiyan.commands.*;
import project.kristiyan.commands.music.*;
import project.kristiyan.database.Database;
import project.kristiyan.listeners.GuildListener;
import project.kristiyan.utilities.Utility;

public class App {
    public static JDA jda;
    public static Database database;
    public static Utility utility;


    public static void main(String[] args) throws Exception {
        database = new Database();
        utility = new Utility();

        JDABuilder builder = JDABuilder.createDefault(
                System.getenv("DISCORD_BOT_TOKEN"),
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_VOICE_STATES);

        jda = builder.build();

        jda.addEventListener(new GuildListener(),
                new UnsubscribeSlashCommand(),
                new SubscribersSlashCommand(),
                new SubscribeSlashCommand(),
                new MusicCommand(),
                new PauseCommand(),
                new ResumeCommand(),
                new SkipCommand(),
                new StopCommand(),
                new QueueCommand(),
                new VolumeCommand()
        );

        jda.awaitReady();

        utility.runTimer();
    }
}
