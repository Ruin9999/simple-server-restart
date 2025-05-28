package dev.shunjieyong.simpleServerRestart;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RestartHelper {
    private RestartHelper() {}

    /// BasicallySolo's code referenced from [...](https://modrinth.com/mod/basicallyrestart)
    public static void restart(MinecraftServer server, String kickMessage) {
        if (SimpleServerRestart.config.runRestartScript) {

            Path workingDirectory;
            try { workingDirectory = Path.of(".").toRealPath(); }
            catch (IOException e) {
                SimpleServerRestart.LOGGER.error("Error resolving working directory.");
                return;
            }

            Path fullPath = workingDirectory.resolve(SimpleServerRestart.config.restartScriptPath);
            if (!Files.exists(fullPath)) {
                SimpleServerRestart.LOGGER.error("Restart script not found.");
                return;
            }

            String command;
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) { command = String.format("cmd /c start \"\" \"%s\"", SimpleServerRestart.config.restartScriptPath); }
            else if (os.contains("nix") || os.contains("nux")) { command = String.format("bash \"%s\"", SimpleServerRestart.config.restartScriptPath); }
            else if (os.contains("mac")) { command = String.format("bash \"%s\"", SimpleServerRestart.config.restartScriptPath); }
            else {
                SimpleServerRestart.LOGGER.error("Error running restart script. Operating system not supported.");
                return;
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Runtime.getRuntime().exec(command, null, workingDirectory.toFile());
                    SimpleServerRestart.LOGGER.info("Executed restart script.");
                } catch (IOException ignored) {
                    SimpleServerRestart.LOGGER.error("Error occurred trying to run the restart script.");
                }
            }));
        }

        server.getPlayerManager().getPlayerList().forEach(player -> player.networkHandler.disconnect(Text.literal(kickMessage)));
        server.stop(false);
    }
}

