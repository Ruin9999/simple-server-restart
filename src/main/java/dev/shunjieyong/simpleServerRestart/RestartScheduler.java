package dev.shunjieyong.simpleServerRestart;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.server.MinecraftServer;

import java.time.LocalDateTime;

public class RestartScheduler {
    private final RestartService service;

    public RestartScheduler() { this.service = new RestartService(AutoConfig.getConfigHolder(SimpleServerRestartConfig.class).getConfig()); }

    public void onStart(MinecraftServer server) {
        int secondsTillNext = service.config.secondsTillNextRestart;
        if (secondsTillNext < 0) {
            SimpleServerRestart.LOGGER.warn("Automatic restart disabled");
            return;
        }

        LocalDateTime restartTime = LocalDateTime.now().plusSeconds(secondsTillNext);
        SimpleServerRestart.LOGGER.info("Restart scheduled at {}", restartTime);
        service.restartLater(server, secondsTillNext);
    }

    public void onStop(MinecraftServer ignored) {
        service.shutdown();
    }
}
