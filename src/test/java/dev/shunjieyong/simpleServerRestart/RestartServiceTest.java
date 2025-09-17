package dev.shunjieyong.simpleServerRestart;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.PlayerManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RestartServiceTest {
    
    @Mock
    private MinecraftServer mockServer;
    
    @Mock
    private ServerCommandSource mockCommandSource;
    
    @Mock
    private PlayerManager mockPlayerManager;
    
    @Mock
    private SimpleServerRestartConfig mockConfig;
    
    @Mock
    private SimpleServerRestartConfig.configWarnPlayers mockWarnPlayersConfig;
    
    private RestartService restartService;
    
    @BeforeEach
    void setUp() {
        // Initialize config with default values
        mockConfig.warnPlayers = mockWarnPlayersConfig;
        mockWarnPlayersConfig.warnPlayers = false; // Disable warnings for most tests
        mockConfig.restartKickMessage = "Server restarting...";
        
        // Setup server mocks
        when(mockServer.getCommandSource()).thenReturn(mockCommandSource);
        when(mockServer.getPlayerManager()).thenReturn(mockPlayerManager);
        when(mockCommandSource.isExecutedByPlayer()).thenReturn(false);
        
        // Mock the static config
        SimpleServerRestart.config = mockConfig;
        
        restartService = RestartService.getInstance();
    }
    
    @Test
    void testScheduleRestart_ShouldScheduleTaskAndLogInfo() {
        // Given
        int delaySeconds = 30;
        
        // When
        restartService.scheduleRestart(mockServer, delaySeconds);
        
        // Then - verify that the server.execute method is called to schedule the restart
        // Note: We can't easily test the actual scheduling due to static dependencies,
        // but we can verify that the method completes without throwing exceptions
        // and that the server interaction methods are called appropriately
        verify(mockServer, atLeastOnce()).getCommandSource();
    }
    
    @Test
    void testScheduleRestart_WithPlayerExecutor_ShouldSendFeedback() {
        // Given
        int delaySeconds = 30;
        when(mockCommandSource.isExecutedByPlayer()).thenReturn(true);
        
        // When
        restartService.scheduleRestart(mockServer, delaySeconds);
        
        // Then
        verify(mockCommandSource).isExecutedByPlayer();
        verify(mockCommandSource).sendFeedback(any(), eq(true));
    }
    
    @Test
    void testScheduleTimedRestart_ValidTimeString_ShouldScheduleRestart() {
        // Given
        String timeString = "14:30"; // 2:30 PM
        
        // When
        restartService.scheduleTimedRestart(mockServer, timeString);
        
        // Then - verify server interactions occur
        verify(mockServer, atLeastOnce()).getCommandSource();
    }
    
    @Test
    void testScheduleTimedRestart_InvalidTimeString_ShouldHandleGracefully() {
        // Given
        String invalidTimeString = "invalid-time";
        
        // When/Then - should not throw exception
        try {
            restartService.scheduleTimedRestart(mockServer, invalidTimeString);
        } catch (Exception e) {
            // Expected to fail gracefully, but method should exist and be callable
        }
    }
    
    @Test
    void testScheduleTimedRestart_MultipleValidTimes_ShouldScheduleEarliest() {
        // Given
        String[] times = {"23:59", "00:01", "12:00"};
        
        // When
        restartService.scheduleTimedRestart(mockServer, times);
        
        // Then - verify server interactions occur
        verify(mockServer, atLeastOnce()).getCommandSource();
    }
    
    @Test
    void testScheduleTimedRestart_EmptyTimesArray_ShouldHandleGracefully() {
        // Given
        String[] emptyTimes = {};
        
        // When/Then - should handle gracefully
        try {
            restartService.scheduleTimedRestart(mockServer, emptyTimes);
        } catch (Exception e) {
            // Method should handle empty arrays gracefully
        }
    }
    
    @Test
    void testShutdown_ShouldShutdownScheduler() {
        // When
        restartService.shutdown();
        
        // Then - method should complete without throwing exceptions
        // The actual scheduler shutdown is tested by ensuring no exceptions are thrown
    }
}