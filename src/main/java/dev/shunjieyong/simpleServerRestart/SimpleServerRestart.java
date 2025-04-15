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
    private RestartScheduler restartScheduler = null;

    @Override
    public void onInitialize() {
        // Register Config
        LOGGER.info("Initializing Simple Server Restart");
        AutoConfig.register(SimpleServerRestartConfig.class, GsonConfigSerializer::new);

        // Register Events
        this.restartScheduler = new RestartScheduler();
        ServerLifecycleEvents.SERVER_STARTED.register(server -> this.restartScheduler.onStart(server));
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> this.restartScheduler.onStop(server));

        //Register Commands
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
            commandDispatcher.register(RestartCommand.register());
        }));
    }
}
