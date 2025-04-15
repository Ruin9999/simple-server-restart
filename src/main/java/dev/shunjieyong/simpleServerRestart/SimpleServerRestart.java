package dev.shunjieyong.simpleServerRestart;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleServerRestart implements ModInitializer {
    public static final String MOD_ID = "simple-server-restart";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Register Config
        LOGGER.info("Initializing Simple Server Restart");
        AutoConfig.register(SimpleServerRestartConfig.class, GsonConfigSerializer::new);
        SimpleServerRestartConfig config = AutoConfig.getConfigHolder(SimpleServerRestartConfig.class).getConfig();

        // Register Events
        ServerLifecycleEvents.SERVER_STARTED.register(server -> RestartService.getInstance().restartLater(server, config.secondsTillNextRestart));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> RestartService.getInstance().shutdown());

        //Register Commands
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
            commandDispatcher.register(RestartCommand.register());
        }));
    }
}
