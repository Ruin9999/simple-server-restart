package dev.shunjieyong.simpleServerRestart;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.CommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleServerRestart implements ModInitializer {
    public static final String MOD_ID = "simple-server-restart";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static SimpleServerRestartConfig config;

    private static final RestartService restartService = RestartService.getInstance();

    @Override
    public void onInitialize() {
        // Register Config
        LOGGER.info("Initializing Simple Server Restart ({})", MOD_ID);
        AutoConfig.register(SimpleServerRestartConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(SimpleServerRestartConfig.class).getConfig();

        // Register Events
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (config.secondsTillNextRestart > 0) RestartService.getInstance().scheduleRestart(server, config.secondsTillNextRestart);
            else LOGGER.info("Automatic server restart is disabled in config.");
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> RestartService.getInstance().shutdown());
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> {
            AutoConfig.getConfigHolder(SimpleServerRestartConfig.class).load();
            config = AutoConfig.getConfigHolder(SimpleServerRestartConfig.class).getConfig();
            SimpleServerRestart.LOGGER.info("Reloaded config!");
        });

        //Register Commands
        //TODO: REGISTER COMMANDS
    }
}
