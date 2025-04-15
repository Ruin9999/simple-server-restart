package dev.shunjieyong.simpleServerRestart;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RestartService {
    public final SimpleServerRestartConfig config;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "RestartScheduler");
        t.setDaemon(true); // Makes the scheduler a daemon so it shutsdown when the server also shutsdown.
        return t;
    });

    public RestartService(SimpleServerRestartConfig config) { this.config = config; }

    public int restartNow(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        RestartHelper.restart(server, config.restartKickMessage);
        return 1;
    }

    public int restartLater(CommandContext<ServerCommandSource> context) {
        MinecraftServer server = context.getSource().getServer();
        int delaySeconds = IntegerArgumentType.getInteger(context, "delaySeconds");

        scheduler.schedule(() -> server.execute(() -> RestartHelper.restart(server, config.restartKickMessage)), delaySeconds, TimeUnit.SECONDS);
        return 0;
    }

    public void restartLater(MinecraftServer server, int delaySeconds) {
        scheduler.schedule(() -> server.execute(() -> RestartHelper.restart(server, config.restartKickMessage)), delaySeconds, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
