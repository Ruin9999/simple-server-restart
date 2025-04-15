package dev.shunjieyong.simpleServerRestart;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class RestartCommand {
    private static final RestartService service = new RestartService(AutoConfig.getConfigHolder(SimpleServerRestartConfig.class).getConfig());

    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("restart")
                .requires(src -> src.hasPermissionLevel(2))
                .executes(service::restartNow)
                .then(CommandManager.argument("delaySeconds", IntegerArgumentType.integer(1))
                        .executes(service::restartLater));
    }
}