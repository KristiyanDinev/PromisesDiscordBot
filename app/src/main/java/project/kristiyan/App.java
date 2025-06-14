package project.kristiyan;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import project.kristiyan.commands.subscribe.SubscribeSlashCommand;
import project.kristiyan.commands.subscribe.SubscribersSlashCommand;
import project.kristiyan.commands.subscribe.UnsubscribeSlashCommand;
import project.kristiyan.commands.music.*;
import project.kristiyan.database.Database;
import project.kristiyan.database.dao.AdminDao;
import project.kristiyan.database.dao.PromiseDao;
import project.kristiyan.database.dao.ReminderDao;
import project.kristiyan.database.dao.UserDao;
import project.kristiyan.listeners.ButtonListener;
import project.kristiyan.listeners.CommandManager;
import project.kristiyan.listeners.GuildListener;
import project.kristiyan.utilities.EmbedUtility;
import project.kristiyan.services.TimerService;
import project.kristiyan.utilities.Utility;

public class App {
    public final static String playlists = "playlists/";

    public static JDA jda;

    public static Database database;
    public static PromiseDao promiseDao;
    public static ReminderDao reminderDao;
    public static UserDao userDao;
    public static AdminDao adminDao;

    public static Utility utility;
    public static EmbedUtility embedUtility;
    public static TimerService timerService;

    public static void main(String[] args) throws Exception {
        database = new Database();
        promiseDao = new PromiseDao(database.getEntityManager());
        reminderDao = new ReminderDao(database.getEntityManager());
        userDao = new UserDao(database.getEntityManager());
        adminDao = new AdminDao(database.getEntityManager());

        // Default admin user
        adminDao.addAdmin("Kristiyan", 636950962245730324L);

        utility = new Utility();
        embedUtility = new EmbedUtility();

        JDABuilder builder = JDABuilder.createDefault(
                System.getenv("PROMISES_DISCORD_BOT_TOKEN"),
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_VOICE_STATES)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL);

        jda = builder.build();

        jda.addEventListener(new GuildListener(),
                new ButtonListener(),
                new UnsubscribeSlashCommand(),
                new SubscribersSlashCommand(),
                new SubscribeSlashCommand(),
                new MusicCommand(),
                new PauseCommand(),
                new ResumeCommand(),
                new SkipCommand(),
                new StopCommand(),
                new QueueCommand(),
                new VolumeCommand(),
                new CommandManager()
        );

        timerService = new TimerService(jda, promiseDao, reminderDao);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            userDao.close();
            promiseDao.close();
            reminderDao.close();
            adminDao.close();
            database.close();
            timerService.stop();
            jda.shutdown();
        }));

        jda.awaitReady();
        timerService.start();
    }
}
