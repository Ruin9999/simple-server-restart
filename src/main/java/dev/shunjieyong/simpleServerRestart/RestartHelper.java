package dev.shunjieyong.simpleServerRestart;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.io.IOException;

public class RestartHelper {
    private RestartHelper() {}

    //TODO: MAYBE WE CAN MOVE THIS INTO THE RESTARTSERVICE CLASS INSTEAD.
    public static void restart(MinecraftServer server, String kickMessage) {
        if (SimpleServerRestart.config.runRestartScript) {
            SimpleServerRestart.LOGGER.info("Running restart script!");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { Runtime.getRuntime().exec( new String[]{ SimpleServerRestart.config.restartScriptPath}); }
                catch (IOException e) {
                    SimpleServerRestart.LOGGER.error("An error occurred while trying to run the restart script.");
                    throw new RuntimeException(e);
                }
            }));
        }

        server.getPlayerManager().getPlayerList().forEach(player -> player.networkHandler.disconnect(Text.literal(kickMessage)));
        server.stop(false);
    }
}
