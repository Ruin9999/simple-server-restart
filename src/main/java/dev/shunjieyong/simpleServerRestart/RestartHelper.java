package dev.shunjieyong.simpleServerRestart;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.io.IOException;

public class RestartHelper {
    private RestartHelper() {}

    public static void restart(MinecraftServer server, String kickMessage) {
        if (SimpleServerRestart.config.runRestartScript) {
            try { Runtime.getRuntime().exec(new String[]{SimpleServerRestart.config.restartScriptPath}); }
            catch (IOException e) { throw new RuntimeException(e); }
        }

        server.getPlayerManager().getPlayerList().forEach(player -> player.networkHandler.disconnect(Text.literal(kickMessage)));
        server.stop(false);
    }
}
