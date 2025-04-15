package dev.shunjieyong.simpleServerRestart;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

public class RestartHelper {
    private RestartHelper() {}

    public static void restart(MinecraftServer server, String kickMessage) {
        server.getPlayerManager().getPlayerList().forEach(player -> {
            player.networkHandler.disconnect(Text.literal(kickMessage));
        });
        server.stop(false);
    }
}
