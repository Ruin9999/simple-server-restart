package dev.shunjieyong.simpleServerRestart;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class RestartCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register() {
        return CommandManager.literal("restart").executes(context -> execute(context.getSource()));
    }

    public static int execute(ServerCommandSource source) {
        MinecraftServer server = source.getServer();
        SimpleServerRestartConfig config = AutoConfig.getConfigHolder(SimpleServerRestartConfig.class).getConfig();

        server.getPlayerManager().getPlayerList().forEach(player -> player.networkHandler.disconnect(Text.literal(config.restartKickMessage)));
        server.stop(false);
        return 1;
    }
}
