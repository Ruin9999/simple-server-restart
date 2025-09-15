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

            // SECURITY FIX: Validate script path to prevent directory traversal
            String scriptPath = SimpleServerRestart.config.restartScriptPath;
            if (scriptPath == null || scriptPath.trim().isEmpty()) {
                SimpleServerRestart.LOGGER.error("Restart script path is not configured");
                return;
            }
            
            // Normalize the script path to prevent directory traversal attacks
            Path fullPath;
            try {
                fullPath = workingDirectory.resolve(scriptPath.trim()).normalize();
                
                // SECURITY: Ensure the resolved path is still within the working directory
                if (!fullPath.startsWith(workingDirectory)) {
                    SimpleServerRestart.LOGGER.error("Restart script path attempts to access files outside working directory: {}", scriptPath);
                    return;
                }
                
                // Check if file exists and is accessible
                if (!Files.exists(fullPath)) {
                    SimpleServerRestart.LOGGER.error("Restart script not found at: {}", fullPath);
                    return;
                }
                
                if (!Files.isRegularFile(fullPath)) {
                    SimpleServerRestart.LOGGER.error("Restart script path is not a regular file: {}", fullPath);
                    return;
                }
                
            } catch (Exception e) {
                SimpleServerRestart.LOGGER.error("Error validating restart script path '{}': {}", scriptPath, e.getMessage(), e);
                return;
            }

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // Use the full path for security
                command = new String[] { "cmd", "/c", "start", "", fullPath.toString() };
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                // Ensure script is executable on Unix-like systems
                if (!Files.isExecutable(fullPath)) {
                    SimpleServerRestart.LOGGER.error("Restart script is not executable: {}", fullPath);
                    return;
                }
                command = new String[] { "bash", fullPath.toString() };
            } else {
                SimpleServerRestart.LOGGER.error("Error running restart script. Operating system not supported: {}", os);
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
        if (command == null || command.length == 0) {
            SimpleServerRestart.LOGGER.error("Cannot run restart script: command is null or empty");
            return;
        }
        
        if (workingDirectory == null || !Files.exists(workingDirectory)) {
            SimpleServerRestart.LOGGER.error("Cannot run restart script: working directory is invalid");
            return;
        }
        
        try {
            // I don't know why, but logging after the process is started doesn't seem to work,
            // so logging that an attempt is happening seems to be the best that can be done. - Ashley_Cause
            SimpleServerRestart.LOGGER.info("Attempting to run restart script with command: {}", String.join(" ", command));
            
            ProcessBuilder processBuilder = new ProcessBuilder(command)
                .directory(workingDirectory.toFile());
            
            Process process = processBuilder.start();
            SimpleServerRestart.LOGGER.info("Restart script process started with PID: {}", process.pid());
            
        } catch (IOException e) {
            // FIX: Proper exception logging instead of ignoring
            SimpleServerRestart.LOGGER.error("Failed to execute restart script. Command: {}, Working Directory: {}, Error: {}", 
                String.join(" ", command), workingDirectory, e.getMessage(), e);
        } catch (SecurityException e) {
            SimpleServerRestart.LOGGER.error("Security exception when trying to run restart script: {}", e.getMessage(), e);
        } catch (Exception e) {
            SimpleServerRestart.LOGGER.error("Unexpected error when running restart script: {}", e.getMessage(), e);
        }
    }

}

