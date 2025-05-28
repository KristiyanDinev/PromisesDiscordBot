package project.kristiyan.utilities;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import project.kristiyan.App;
import project.kristiyan.database.dao.PromiseDao;
import project.kristiyan.database.dao.ReminderDao;
import project.kristiyan.database.entities.PromiseEntity;
import project.kristiyan.database.entities.ReminderEntity;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
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
        // Cache files once per batch instead of per entity
        List<File> files = App.utility.getFiles("promises/");
        if (files.isEmpty()) {
            return;
        }

        Collections.shuffle(files);
        String promiseContent = null;

        try {
            promiseContent = Files.readString(Paths.get(files.getFirst().getPath()));

        } catch (Exception e) {
            return;
        }

        for (PromiseEntity promiseEntity : promiseEntities) {
            try {
                if (!isTimeToSend(promiseEntity.time)) {
                    continue; // Use continue instead of return to process other entities
                }

                User user_obj = jda.getUserById(promiseEntity.userEntity.id);
                if (user_obj == null) {
                    continue;
                }

                PrivateChannel channel = user_obj.openPrivateChannel()
                        .useCache(false).complete();

                // Make effectively final for lambda
                channel.sendMessageEmbeds(App.utility.buildEmbed(promiseContent))
                        .queue(
                                success -> {} // Success callback
                        );

            } catch (Exception ignored) {
            }
        }
    }

    private void processRemindersBatch(List<ReminderEntity> reminderEntities) {
        // Cache reminder content once per batch
        String reminderContent = null;

        try {
            reminderContent = Files.readString(Paths.get("settings.json"));

        } catch (Exception e) {
            return;
        }

        for (ReminderEntity reminderEntity : reminderEntities) {
            try {
                if (!isTimeToSend(reminderEntity.time)) {
                    continue; // Use continue instead of return
                }

                User user_obj = jda.getUserById(reminderEntity.userEntity.id);
                if (user_obj == null) {
                    continue;
                }

                PrivateChannel channel = user_obj.openPrivateChannel()
                        .useCache(false).complete();

                // Make effectively final for lambda
                channel.sendMessageEmbeds(App.utility.buildEmbed(reminderContent))
                        .queue(
                                success -> {}, // Success callback
                                error -> System.err.println("Failed to send reminder to user " +
                                        reminderEntity.userEntity.id + ": " + error.getMessage())
                        );

            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Checks if the current time matches the specified time string
     * Time format: "HH:mm Timezone" e.g., "12:00 Europe/Sofia"
     *
     * @param timeString The time string in "HH:mm Timezone" format
     * @return true if it's time to send the message, false otherwise
     */
    private boolean isTimeToSend(String timeString) {
        if (!isValidTimeFormat(timeString)) {
            return false;
        }

        try {
            // Parse the time string "HH:mm Timezone"
            String[] parts = timeString.trim().split(" ", 2);
            if (parts.length != 2) {
                System.err.println("Invalid time format: " + timeString + " (expected: HH:mm Timezone)");
                return false;
            }

            String timeStr = parts[0]; // HH:mm
            String timezoneStr = parts[1]; // Timezone

            // Parse the time and timezone
            LocalTime targetTime = LocalTime.parse(timeStr, TIME_FORMATTER);
            ZoneId timezone = ZoneId.of(timezoneStr);

            // Get current time in the specified timezone
            ZonedDateTime nowInTimezone = ZonedDateTime.now(timezone);
            LocalTime currentTime = nowInTimezone.toLocalTime();

            // Check if current time matches target time (within the same minute)
            return currentTime.getHour() == targetTime.getHour() &&
                    currentTime.getMinute() == targetTime.getMinute();

        } catch (Exception e) {
            System.err.println("Error checking time: " + e.getMessage());
            return false;
        }
    }

    /**
     * Utility method to validate time format
     * @param timeString The time string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidTimeFormat(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return false;
        }

        try {
            String[] parts = timeString.trim().split(" ", 2);
            if (parts.length != 2) {
                return false;
            }

            // Validate time format
            LocalTime.parse(parts[0], TIME_FORMATTER);

            // Validate timezone
            ZoneId.of(parts[1]);

            return true;

        } catch (Exception e) {
            return false;
        }
    }
}