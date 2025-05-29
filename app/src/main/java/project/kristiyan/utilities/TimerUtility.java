package project.kristiyan.utilities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import project.kristiyan.App;
import project.kristiyan.database.dao.PromiseDao;
import project.kristiyan.database.dao.ReminderDao;
import project.kristiyan.database.entities.PromiseEntity;
import project.kristiyan.database.entities.ReminderEntity;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.*;

public class TimerUtility {
    private final JDA jda;
    private final PromiseDao promiseDao;
    private final ReminderDao reminderDao;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService messageExecutor;
    private ScheduledFuture<?> currentTask;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final int CHECK_INTERVAL_MINUTES = 1;
    private static final int BATCH_SIZE = 100; // Process in batches to avoid memory issues

    public TimerUtility(JDA jda, PromiseDao promiseDao, ReminderDao reminderDao) {
        this.jda = jda;
        this.promiseDao = promiseDao;
        this.reminderDao = reminderDao;

        // Single thread for scheduling to maintain consistency
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Timer-Scheduler");
            t.setDaemon(true);
            return t;
        });

        // Separate thread pool for message sending to avoid blocking
        this.messageExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "Timer-Sender");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Starts the Schedule
     */
    public synchronized void start() {
        if (currentTask != null && !currentTask.isDone()) {
            return;
        }

        currentTask = scheduler.scheduleAtFixedRate(
                this::executeSendingMessages,
                0, // Start immediately
                CHECK_INTERVAL_MINUTES,
                TimeUnit.MINUTES
        );
    }

    /**
     * Stops the timer and cancels any pending executions.
     */
    public synchronized void stop() {
        if (currentTask != null) {
            currentTask.cancel(false);
            currentTask = null;
        }
        scheduler.shutdown();
        messageExecutor.shutdown();

        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            if (!messageExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                messageExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduler.shutdownNow();
            messageExecutor.shutdownNow();
        }
    }

    private void executeSendingMessages() {
        try {
            // Process promises and reminders in separate threads
            CompletableFuture<Void> promisesTask = CompletableFuture.runAsync(this::processAllPromises, messageExecutor);
            CompletableFuture<Void> remindersTask = CompletableFuture.runAsync(this::processAllReminders, messageExecutor);

            // Wait for both tasks to complete with timeout
            CompletableFuture.allOf(promisesTask, remindersTask)
                    .get(30, TimeUnit.SECONDS); // Add timeout to prevent hanging

        } catch (Exception ignored) {
        }
    }

    private void processAllPromises() {
        try {
            int page = 1;
            List<PromiseEntity> batch;

            do {
                batch = promiseDao.getUsers(page);
                if (!batch.isEmpty()) {
                    processPromisesBatch(batch);
                    page++;
                }
            } while (!batch.isEmpty());

        } catch (Exception ignored) {
        }
    }

    private void processAllReminders() {
        try {
            int page = 1;
            List<ReminderEntity> batch;

            do {
                batch = reminderDao.getUsers(page);
                if (!batch.isEmpty()) {
                    processRemindersBatch(batch);
                    page++;
                }
            } while (!batch.isEmpty());

        } catch (Exception ignored) {
        }
    }

    private void processPromisesBatch(List<PromiseEntity> promiseEntities) {
        List<File> files = App.utility.getFiles("promises/");
        if (files.isEmpty()) {
            return;
        }

        File file = files.get((int)(Math.random() * files.size()));
        String promiseContent = null;
        try {
            promiseContent = Files.readString(Paths.get(file.getPath()));

        } catch (Exception e) {
            return;
        }

        for (PromiseEntity promiseEntity : promiseEntities) {
            try {
                if (isNotTimeToSend(promiseEntity.time)) {
                    continue;
                }

                User user2 = getUserById(promiseEntity.userEntity.id);
                User user = jda.getUserById(promiseEntity.userEntity.id);
                if (user == null) {
                    continue;
                }

                PrivateChannel channel = user.openPrivateChannel()
                        .useCache(false).complete();
                channel.sendMessageEmbeds(App.utility.buildEmbed(promiseContent))
                        .queue();

            } catch (Exception ignored) {
            }
        }
    }

    private void processRemindersBatch(List<ReminderEntity> reminderEntities) {
        for (ReminderEntity reminderEntity : reminderEntities) {
            try {
                if (isNotTimeToSend(reminderEntity.time)) {
                    continue;
                }

                User user = getUserById(reminderEntity.userEntity.id);
                if (user == null) {
                    continue;
                }

                PrivateChannel channel = user.openPrivateChannel()
                        .useCache(false).complete();
                /*
                channel.sendMessageEmbeds(App.utility.buildEmbed(reminderContent))
                        .queue();*/

                channel.sendMessage("Hello. Reminder Service.")
                        .queue();

            } catch (Exception ignored) {
            }
        }
    }

    private User getUserById(long id) {
        for (Guild g : jda.getGuilds()) {
            Member member = g.getMemberById(id);
            if (member != null) {
                return member.getUser();
            }
        }
        return null;
    }

    /**
     * Checks if the current time matches the specified time string
     * Time format: "HH:mm Timezone" e.g., "12:00 Europe/Sofia"
     *
     * @param timeString The time string in "HH:mm Timezone" format
     * @return true if it's time to send the message, false otherwise
     */
    private boolean isNotTimeToSend(String timeString) {
        if (isInvalidTimeFormat(timeString)) {
            return true;
        }

        try {
            // Parse the time string "HH:mm Timezone"
            String[] parts = timeString.trim().split(" ", 2);

            // Parse the time and timezone
            LocalTime targetTime = LocalTime.parse(parts[0], TIME_FORMATTER);

            // Get current time in the specified timezone
            LocalTime currentTimeInZone = ZonedDateTime.now(ZoneId.of(parts[1])).toLocalTime();

            // Check if current time matches target time (within the same minute)
            return currentTimeInZone.getHour() != targetTime.getHour() &&
                    currentTimeInZone.getMinute() != targetTime.getMinute();

        } catch (Exception ignore) {
            return true;
        }
    }

    /**
     * Utility method to validate time format
     * @param timeString The time string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isInvalidTimeFormat(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return true;
        }

        try {
            String[] parts = timeString.trim().split(" ", 2);
            if (parts.length != 2) {
                return true;
            }

            String time = parts[0];
            if (time.charAt(1) == ':') {
                time = "0" + time;
            }

            // Validate time format
            LocalTime.parse(time, TIME_FORMATTER);

            // Validate timezone
            ZoneId.of(parts[1]);

            return false;

        } catch (Exception e) {
            return true;
        }
    }
}