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

        String[] command;
        Path workingDirectory;

        if (SimpleServerRestart.config.runRestartScript) {

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

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                command = new String[] { "cmd", "/c", "start", "", SimpleServerRestart.config.restartScriptPath };
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                command = new String[] { "bash", SimpleServerRestart.config.restartScriptPath };
            } else {
                SimpleServerRestart.LOGGER.error("Error running restart script. Operating system not supported.");
                return;
            }

            server.getPlayerManager().getPlayerList().forEach(player -> player.networkHandler.disconnect(Text.literal(kickMessage)));

            if (SimpleServerRestart.config.stopServer) Runtime.getRuntime().addShutdownHook(new Thread(() -> runRestartScript(command, workingDirectory)));
            else runRestartScript(command, workingDirectory);
        }
        
        if (!SimpleServerRestart.config.runRestartScript) server.getPlayerManager().getPlayerList().forEach(player -> player.networkHandler.disconnect(Text.literal(kickMessage)));
        server.stop(false);
    }

    public static void runRestartScript(String[] command, Path workingDirectory) {
        try {
            // I don't know why, but logging after the process is started doesn't seem to work,
            // so logging that an attempt is happening seems to be the best that can be done. - Ashley_Cause
            SimpleServerRestart.LOGGER.info("Trying to run restart script.");
            new ProcessBuilder(command)
                .directory(workingDirectory.toFile())
                .start();
        } catch (IOException ignored) {
            SimpleServerRestart.LOGGER.error("Error occurred trying to run the restart script.");
        }
    }

}

