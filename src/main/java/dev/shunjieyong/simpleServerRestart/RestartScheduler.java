package dev.shunjieyong.simpleServerRestart;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.server.MinecraftServer;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RestartScheduler {
    private ScheduledExecutorService scheduler = null;

    public void onStart(MinecraftServer server) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        int secondsTillNextRestart = AutoConfig.getConfigHolder(SimpleServerRestartConfig.class).getConfig().secondsTillNextRestart;

        if (secondsTillNextRestart < 0) {
            SimpleServerRestart.LOGGER.warn("Automatic restart is disabled (secondsTillNextRestart < 0)");
            if (this.scheduler != null) this.scheduler.shutdown();
            this.scheduler = null;
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledRestartTime = now.plusSeconds(secondsTillNextRestart);

        SimpleServerRestart.LOGGER.info("Restart scheduled at: {}", scheduledRestartTime);
        scheduler.schedule(() -> server.execute(() -> RestartCommand.execute(server.getCommandSource())), secondsTillNextRestart, TimeUnit.SECONDS);
    }

    public void onStop(MinecraftServer ignored) {
        if (this.scheduler == null) return;
        this.scheduler.shutdownNow();
        try {
            if (!this.scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                SimpleServerRestart.LOGGER.error("Restart scheduler did not terminate cleanly");
            }
        } catch (InterruptedException ie) {
            this.scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
