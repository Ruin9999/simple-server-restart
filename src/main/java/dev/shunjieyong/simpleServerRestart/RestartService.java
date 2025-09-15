package dev.shunjieyong.simpleServerRestart;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;


import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.Duration;


// Singleton
public class RestartService {
    private static final RestartService INSTANCE = new RestartService();

    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> currentTask = null;

    public RestartService() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "RestartScheduler");
            t.setDaemon(true);
            return t;
        });
    }

    public static RestartService getInstance() { return INSTANCE; }

    public synchronized void scheduleRestart(MinecraftServer server, int delaySeconds) {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(false);
            currentTask = null;
        }

        LocalDateTime restartTime = LocalDateTime.now().plusSeconds(delaySeconds);
        SimpleServerRestart.LOGGER.info("Restart scheduled at {}", restartTime);
        
        // FIX: Add null check for command source to prevent NPE
        try {
            if (server.getCommandSource() != null && server.getCommandSource().isExecutedByPlayer()) {
                server.getCommandSource().sendFeedback(() -> Text.literal("Restart scheduled at " + restartTime), true);
            }
        } catch (Exception e) {
            SimpleServerRestart.LOGGER.warn("Failed to send restart confirmation to player: {}", e.getMessage());
        }
        
        currentTask = scheduler.schedule(() -> server.execute(() -> RestartHelper.restart(server, SimpleServerRestart.config.restartKickMessage)), delaySeconds, TimeUnit.SECONDS);
        if (SimpleServerRestart.config.warnPlayers.warnPlayers) scheduleRestartWarnings(server, restartTime);
    }

    public void scheduleTimedRestart(MinecraftServer server, String time) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTime = LocalDateTime.now().with(LocalTime.parse(time));
        if (now.isAfter(targetTime)) targetTime = targetTime.plusDays(1);
        int delay = (int)Duration.between(now, targetTime).toSeconds();

        scheduleRestart(server, delay);
    }

    public void scheduleTimedRestart(MinecraftServer server, String[] times) {
        LocalDateTime now = LocalDateTime.now();

        try {
            LocalDateTime targetTime = Stream.of(times)
                .map(LocalTime::parse)
                .map(time -> {
                    LocalDateTime thisTime = now.with(time);
                    if (thisTime.isBefore(now)) thisTime = thisTime.plusDays(1);
                    return thisTime;
                })
                .min(Comparator.comparing(time -> Duration.between(now, time).toSeconds()))
                .orElse(null);

            int delay = (int) Duration.between(now, targetTime).toSeconds();
            scheduleRestart(server, delay);

        } catch (DateTimeParseException err) {
            SimpleServerRestart.LOGGER.error("No valid times were found.");
            return;
        }
    }

    public void scheduleRestartWarnings(MinecraftServer server, LocalDateTime restartTime) {
        String[] intervals = SimpleServerRestart.config.warnPlayers.warnPlayerIntervals;
        for (String interval : intervals) {
            if (interval == null || interval.trim().isEmpty()) {
                continue; // Skip null or empty intervals
            }
            
            try {
                Duration duration = setTimeUnit(interval);
                long delay = Duration.between(LocalDateTime.now(), restartTime.minus(duration)).getSeconds();
                if (delay > 0) {
                    scheduler.schedule(() -> {
                        server.getPlayerManager().broadcast(Text.literal(
                            String.format(SimpleServerRestart.config.warnPlayers.warnPlayerMessage, interval)), false);
                    }, delay, TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                SimpleServerRestart.LOGGER.warn("Failed to schedule warning for interval '{}': {}", interval, e.getMessage());
            }
        }
    }

    // Helper to parse the unit from "5m", "10m", etc.
    private Duration setTimeUnit(String interval) {
        if (interval == null || interval.trim().isEmpty()) {
            throw new IllegalArgumentException("Interval cannot be null or empty");
        }
        
        ChronoUnit unit;
        interval = interval.trim().toLowerCase();
        
        // FIX: Validate interval length to prevent StringIndexOutOfBoundsException
        if (interval.length() == 0) {
            throw new IllegalArgumentException("Interval cannot be empty after trimming");
        }
        
        if (interval.endsWith("h")) {
            unit = ChronoUnit.HOURS;
        } else if (interval.endsWith("m")) {
            unit = ChronoUnit.MINUTES;
        } else if (interval.endsWith("s")) {
            unit = ChronoUnit.SECONDS;
        } else {
            // If no unit specified, assume seconds
            try {
                return Duration.of(Integer.parseInt(interval), ChronoUnit.SECONDS);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid interval format: " + interval, e);
            }
        }
        
        try {
            // FIX: Proper bounds checking before substring operation
            String numberPart = interval.substring(0, interval.length() - 1);
            int value = Integer.parseInt(numberPart);
            
            if (value <= 0) {
                throw new IllegalArgumentException("Interval value must be positive: " + value);
            }
            
            return Duration.of(value, unit);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number in interval: " + interval, e);
        }
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
