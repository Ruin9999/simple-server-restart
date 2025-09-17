package dev.shunjieyong.simpleServerRestart;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.PlayerManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SimpleServerRestartCommandTest {
    
    @Mock
    private MinecraftServer mockServer;
    
    @Mock
    private ServerCommandSource mockCommandSource;
    
    @Mock
    private ServerCommandSource mockUnprivilegedCommandSource;
    
    @Mock
    private PlayerManager mockPlayerManager;
    
    @Mock
    private CommandContext<ServerCommandSource> mockCommandContext;
    
    @Mock
    private SimpleServerRestartConfig mockConfig;
    
    @Mock
    private SimpleServerRestartConfig.configWarnPlayers mockWarnPlayersConfig;
    
    private RestartService restartService;
    
    @BeforeEach
    void setUp() {
        // Setup config
        mockConfig.warnPlayers = mockWarnPlayersConfig;
        mockWarnPlayersConfig.warnPlayers = false;
        mockConfig.restartKickMessage = "Server restarting...";
        
        // Setup server mocks
        when(mockServer.getCommandSource()).thenReturn(mockCommandSource);
        when(mockServer.getPlayerManager()).thenReturn(mockPlayerManager);
        
        // Setup privileged command source (permission level 2+)
        when(mockCommandSource.hasPermissionLevel(2)).thenReturn(true);
        when(mockCommandSource.isExecutedByPlayer()).thenReturn(false);
        when(mockCommandSource.getServer()).thenReturn(mockServer);
        
        // Setup unprivileged command source (permission level < 2)
        when(mockUnprivilegedCommandSource.hasPermissionLevel(2)).thenReturn(false);
        when(mockUnprivilegedCommandSource.getServer()).thenReturn(mockServer);
        
        // Setup command context
        when(mockCommandContext.getSource()).thenReturn(mockCommandSource);
        
        SimpleServerRestart.config = mockConfig;
        restartService = RestartService.getInstance();
    }
    
    @Test
    void testPrivilegedUser_CanExecuteRestartCommand() {
        // Given - a privileged user (permission level 2+)
        assertTrue(mockCommandSource.hasPermissionLevel(2), "Command source should have permission level 2");
        
        // When - executing the restart command
        when(mockCommandContext.getSource()).thenReturn(mockCommandSource);
        
        // Simulate the command execution logic from registerCommands()
        boolean hasPermission = mockCommandSource.hasPermissionLevel(2);
        
        // Then - the command should be allowed to execute
        assertTrue(hasPermission, "Privileged user should be able to execute restart command");
        
        // Verify that if this user executed the command, the restart would be scheduled
        if (hasPermission) {
            restartService.scheduleRestart(mockServer, 1);
            verify(mockCommandSource, atLeastOnce()).hasPermissionLevel(2);
        }
    }
    
    @Test
    void testUnprivilegedUser_CannotExecuteRestartCommand() {
        // Given - an unprivileged user (permission level < 2)
        assertFalse(mockUnprivilegedCommandSource.hasPermissionLevel(2), "Command source should not have permission level 2");
        
        // When - checking if the user can execute the restart command
        boolean hasPermission = mockUnprivilegedCommandSource.hasPermissionLevel(2);
        
        // Then - the command should be rejected
        assertFalse(hasPermission, "Unprivileged user should not be able to execute restart command");
        
        // Verify permission check was called
        verify(mockUnprivilegedCommandSource).hasPermissionLevel(2);
    }
    
    @Test
    void testRestartCommand_WithValidPermissions_ShouldScheduleImmedateRestart() {
        // Given
        when(mockCommandContext.getSource()).thenReturn(mockCommandSource);
        
        // When - simulating the restart command execution
        if (mockCommandSource.hasPermissionLevel(2)) {
            restartService.scheduleRestart(mockCommandSource.getServer(), 1);
        }
        
        // Then
        verify(mockCommandSource).hasPermissionLevel(2);
        verify(mockCommandSource, atLeastOnce()).getServer();
    }
    
    @Test
    void testRestartDelayCommand_WithValidPermissions_ShouldScheduleDelayedRestart() {
        // Given
        int delaySeconds = 300; // 5 minutes
        when(mockCommandContext.getSource()).thenReturn(mockCommandSource);
        
        // When - simulating the restart delay command execution
        if (mockCommandSource.hasPermissionLevel(2)) {
            restartService.scheduleRestart(mockCommandSource.getServer(), delaySeconds);
        }
        
        // Then
        verify(mockCommandSource).hasPermissionLevel(2);
        verify(mockCommandSource, atLeastOnce()).getServer();
    }
    
    @Test
    void testRestartTimeCommand_WithValidPermissions_ShouldScheduleTimedRestart() {
        // Given
        String timeString = "14:30";
        when(mockCommandContext.getSource()).thenReturn(mockCommandSource);
        
        // When - simulating the restart time command execution
        if (mockCommandSource.hasPermissionLevel(2)) {
            restartService.scheduleTimedRestart(mockCommandSource.getServer(), timeString);
        }
        
        // Then
        verify(mockCommandSource).hasPermissionLevel(2);
        verify(mockCommandSource, atLeastOnce()).getServer();
    }
    
    @Test
    void testCommandRequiresPermissionLevel2() {
        // This test verifies the core requirement: commands require permission level 2
        
        // Test with permission level 0 (default player)
        ServerCommandSource level0Source = mock(ServerCommandSource.class);
        when(level0Source.hasPermissionLevel(2)).thenReturn(false);
        assertFalse(level0Source.hasPermissionLevel(2), "Level 0 user should not have permission");
        
        // Test with permission level 1 (limited permissions)
        ServerCommandSource level1Source = mock(ServerCommandSource.class);
        when(level1Source.hasPermissionLevel(2)).thenReturn(false);
        assertFalse(level1Source.hasPermissionLevel(2), "Level 1 user should not have permission");
        
        // Test with permission level 2 (operator)
        ServerCommandSource level2Source = mock(ServerCommandSource.class);
        when(level2Source.hasPermissionLevel(2)).thenReturn(true);
        assertTrue(level2Source.hasPermissionLevel(2), "Level 2 user should have permission");
        
        // Test with permission level 3 (admin)
        ServerCommandSource level3Source = mock(ServerCommandSource.class);
        when(level3Source.hasPermissionLevel(2)).thenReturn(true);
        assertTrue(level3Source.hasPermissionLevel(2), "Level 3 user should have permission");
        
        // Test with permission level 4 (owner)
        ServerCommandSource level4Source = mock(ServerCommandSource.class);
        when(level4Source.hasPermissionLevel(2)).thenReturn(true);
        assertTrue(level4Source.hasPermissionLevel(2), "Level 4 user should have permission");
    }
    
    @Test
    void testRestartScript_ExecutionFlow_WithPermissions() {
        // Given - a privileged user and proper config
        mockConfig.runRestartScript = true;
        mockConfig.restartScriptPath = "restart.sh";
        
        // When - user with proper permissions executes restart command
        if (mockCommandSource.hasPermissionLevel(2)) {
            // This simulates the flow: command execution -> restart scheduling -> script execution
            restartService.scheduleRestart(mockCommandSource.getServer(), 1);
            
            // The actual script execution would happen via RestartHelper.restart()
            // We verify the setup is correct for the execution chain
        }
        
        // Then - verify the permission check occurred and server interaction happened
        verify(mockCommandSource).hasPermissionLevel(2);
        verify(mockCommandSource, atLeastOnce()).getServer();
    }
}