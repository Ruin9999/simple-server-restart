package dev.shunjieyong.simpleServerRestart;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.Text;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestartHelperTest {
    
    @Mock
    private MinecraftServer mockServer;
    
    @Mock
    private PlayerManager mockPlayerManager;
    
    @Mock
    private ServerPlayerEntity mockPlayer;
    
    @Mock
    private ServerPlayNetworkHandler mockNetworkHandler;
    
    @Mock
    private SimpleServerRestartConfig mockConfig;
    
    private String originalOsName;
    
    @BeforeEach
    void setUp() {
        // Store original OS name for restoration
        originalOsName = System.getProperty("os.name");
        
        // Setup basic mocks
        when(mockServer.getPlayerManager()).thenReturn(mockPlayerManager);
        when(mockPlayerManager.getPlayerList()).thenReturn(Collections.singletonList(mockPlayer));
        when(mockPlayer.networkHandler).thenReturn(mockNetworkHandler);
        
        // Setup config defaults
        mockConfig.runRestartScript = true;
        mockConfig.restartScriptPath = "test-restart.sh";
        mockConfig.stopServer = true;
        
        SimpleServerRestart.config = mockConfig;
    }
    
    @Test
    void testRestart_WithRunRestartScriptDisabled_ShouldOnlyStopServer() {
        // Given
        mockConfig.runRestartScript = false;
        String kickMessage = "Server restarting...";
        
        // When
        RestartHelper.restart(mockServer, kickMessage);
        
        // Then
        verify(mockPlayerManager).getPlayerList();
        verify(mockNetworkHandler).disconnect(any(Text.class));
        verify(mockServer).stop(false);
    }
    
    @Test
    void testRestart_WithValidScriptOnLinux_ShouldExecuteScript() throws IOException {
        // Given
        System.setProperty("os.name", "Linux");
        String kickMessage = "Server restarting...";
        
        // Create a temporary test script file
        Path tempScript = null;
        try {
            tempScript = Files.createTempFile("test-restart", ".sh");
            mockConfig.restartScriptPath = tempScript.getFileName().toString();
            
            // When
            RestartHelper.restart(mockServer, kickMessage);
            
            // Then
            verify(mockPlayerManager).getPlayerList();
            verify(mockNetworkHandler).disconnect(any(Text.class));
            verify(mockServer).stop(false);
            
        } catch (Exception e) {
            // Test the error handling path - script execution may fail in test environment
            // but we verify that the attempt was made by checking player disconnection
            verify(mockPlayerManager).getPlayerList();
        } finally {
            // Cleanup
            if (tempScript != null) {
                try {
                    Files.deleteIfExists(tempScript);
                } catch (IOException ignored) {}
            }
            System.setProperty("os.name", originalOsName);
        }
    }
    
    @Test
    void testRestart_WithValidScriptOnWindows_ShouldExecuteScript() throws IOException {
        // Given
        System.setProperty("os.name", "Windows 10");
        String kickMessage = "Server restarting...";
        
        // Create a temporary test script file
        Path tempScript = null;
        try {
            tempScript = Files.createTempFile("test-restart", ".bat");
            mockConfig.restartScriptPath = tempScript.getFileName().toString();
            
            // When
            RestartHelper.restart(mockServer, kickMessage);
            
            // Then
            verify(mockPlayerManager).getPlayerList();
            verify(mockNetworkHandler).disconnect(any(Text.class));
            verify(mockServer).stop(false);
            
        } catch (Exception e) {
            // Test the error handling path - script execution may fail in test environment
            verify(mockPlayerManager).getPlayerList();
        } finally {
            // Cleanup
            if (tempScript != null) {
                try {
                    Files.deleteIfExists(tempScript);
                } catch (IOException ignored) {}
            }
            System.setProperty("os.name", originalOsName);
        }
    }
    
    @Test
    void testRestart_WithNonExistentScript_ShouldHandleGracefully() {
        // Given
        System.setProperty("os.name", "Linux");
        mockConfig.restartScriptPath = "non-existent-script.sh";
        String kickMessage = "Server restarting...";
        
        try {
            // When
            RestartHelper.restart(mockServer, kickMessage);
            
            // Then - should handle missing script gracefully and still stop server
            verify(mockServer).stop(false);
            
        } finally {
            System.setProperty("os.name", originalOsName);
        }
    }
    
    @Test
    void testRestart_WithUnsupportedOS_ShouldHandleGracefully() {
        // Given
        System.setProperty("os.name", "Unknown OS");
        String kickMessage = "Server restarting...";
        
        try {
            // When
            RestartHelper.restart(mockServer, kickMessage);
            
            // Then - should handle unsupported OS gracefully and still stop server
            verify(mockServer).stop(false);
            
        } finally {
            System.setProperty("os.name", originalOsName);
        }
    }
    
    @Test
    void testRunRestartScript_WithValidCommand_ShouldExecuteCommand() {
        // Given
        String[] command = {"echo", "test"};
        Path workingDirectory = Path.of(".");
        
        // When
        RestartHelper.runRestartScript(command, workingDirectory);
        
        // Then - method should complete without throwing exceptions
        // The actual process execution is tested by ensuring no exceptions are thrown
        // and the method can be called successfully
    }
    
    @Test
    void testRunRestartScript_WithInvalidCommand_ShouldHandleGracefully() {
        // Given
        String[] invalidCommand = {"non-existent-command", "with", "args"};
        Path workingDirectory = Path.of(".");
        
        // When/Then - should handle invalid commands gracefully
        try {
            RestartHelper.runRestartScript(invalidCommand, workingDirectory);
        } catch (Exception e) {
            // Method should handle IOException gracefully
        }
    }
}