package project.kristiyan;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import project.kristiyan.commands.SubscribeSlashCommand;
import project.kristiyan.commands.SubscribersSlashCommand;
import project.kristiyan.commands.UnsubscribeSlashCommand;
import project.kristiyan.commands.music.*;
import project.kristiyan.database.Database;
import project.kristiyan.database.dao.PromiseDao;
import project.kristiyan.database.dao.ReminderDao;
import project.kristiyan.database.dao.UserDao;
import project.kristiyan.listeners.GuildListener;
import project.kristiyan.utilities.TimerUtility;
import project.kristiyan.utilities.Utility;

public class App {
    public final static String playlists = "playlists/";

    public static JDA jda;
    public static Database database;
    public static PromiseDao promiseDao;
    public static ReminderDao reminderDao;
    public static UserDao userDao;
    public static Utility utility;
    public static TimerUtility timerUtility;

    public static void main(String[] args) throws Exception {
        database = new Database();
        promiseDao = new PromiseDao(database.getEntityManager());
        reminderDao = new ReminderDao(database.getEntityManager());
        userDao = new UserDao(database.getEntityManager());

        utility = new Utility();

        JDABuilder builder = JDABuilder.createDefault(
                System.getenv("DISCORD_BOT_TOKEN"),
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_VOICE_STATES)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL);

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

        timerUtility = new TimerUtility(jda, promiseDao, reminderDao);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            userDao.close();
            promiseDao.close();
            reminderDao.close();
            database.close();
            timerUtility.stop();
            jda.shutdown();
        }));

        timerUtility.start();
    }
}
