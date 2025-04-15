package dev.shunjieyong.simpleServerRestart;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RestartService {
    private static final RestartService INSTANCE = new RestartService(AutoConfig.getConfigHolder(SimpleServerRestartConfig.class).getConfig());

    public final SimpleServerRestartConfig config;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> currentTask = null;

    public RestartService(SimpleServerRestartConfig config) {
        this.config = config;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "RestartScheduler");
            t.setDaemon(true);
            return t;
        });
    }

    public static RestartService getInstance() { return INSTANCE; }

    public int restartNow(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        RestartHelper.restart(server, config.restartKickMessage);
        return 1;
    }

    public synchronized int restartLater(CommandContext<ServerCommandSource> context) { // Overloaded function
        MinecraftServer server = context.getSource().getServer();
        int delaySeconds = IntegerArgumentType.getInteger(context, "delaySeconds");
        restartLater(server, delaySeconds);
        return 1;
    }

    public synchronized void restartLater(MinecraftServer server, int delaySeconds) { // Main function that we want to use to schedule restarts
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(false);
            currentTask = null;
        }

        LocalDateTime restartTime = LocalDateTime.now().plusSeconds(delaySeconds);
        server.getCommandSource().sendFeedback(() -> Text.literal("Restart scheduled at " + restartTime), true);
        currentTask = scheduler.schedule(() -> server.execute(() -> RestartHelper.restart(server, config.restartKickMessage)), delaySeconds, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
