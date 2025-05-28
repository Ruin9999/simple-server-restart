package dev.shunjieyong.simpleServerRestart;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
        if (server.getCommandSource().isExecutedByPlayer()) server.getCommandSource().sendFeedback(() -> Text.literal("Restart scheduled at " + restartTime), true);
        currentTask = scheduler.schedule(() -> server.execute(() -> RestartHelper.restart(server, SimpleServerRestart.config.restartKickMessage)), delaySeconds, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
