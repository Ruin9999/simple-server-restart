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
        registerConfig();
        registerEvents();
        registerCommands();
    }

    private void registerConfig() {
        LOGGER.info("Initializing Simple Server Restart ({})", MOD_ID);
        AutoConfig.register(SimpleServerRestartConfig.class, GsonConfigSerializer::new);
        config = AutoConfig.getConfigHolder(SimpleServerRestartConfig.class).getConfig();
    }

    private void registerEvents() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> restartService.shutdown());

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (config.secondsTillNextRestart > 0) restartService.scheduleRestart(server, config.secondsTillNextRestart);
            else LOGGER.info("Automatic server restart is disabled in config.");
        });

        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> {
            AutoConfig.getConfigHolder(SimpleServerRestartConfig.class).load();
            config = AutoConfig.getConfigHolder(SimpleServerRestartConfig.class).getConfig();
            SimpleServerRestart.LOGGER.info("Reloaded config!");

            if (config.rescheduleOnReload) restartService.scheduleRestart(server, config.secondsTillNextRestart);
        });
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
            commandDispatcher.register(CommandManager.literal("restart")
                .requires(src -> src.hasPermissionLevel(2))
                .executes(ctx -> {
                    restartService.scheduleRestart(ctx.getSource().getServer(), 1);
                    return 1;
                })
                .then(CommandManager.argument("delayDuration", IntegerArgumentType.integer())
                    .requires(src -> src.hasPermissionLevel(2))
                    .executes(ctx -> {
                        restartService.scheduleRestart(ctx.getSource().getServer(), IntegerArgumentType.getInteger(ctx, "delayDuration"));
                        return 1;
                    })));
        }));
    }
}
